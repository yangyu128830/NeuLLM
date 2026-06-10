package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.model.classroom.ParsedSubTask;
import com.neusoft.edu.neullmdev.model.classroom.ParsedTaskDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskDocumentParserService {

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?:课堂任务|任务标题)[：:]\\s*(.+)");
    private static final Pattern DESC_PATTERN = Pattern.compile("(?:任务说明|任务描述)[：:]\\s*(.+)");
    private static final Pattern SECTION_PATTERN = Pattern.compile(
            "^(?:[一二三四五六七八九十]+、|\\d+[.、])\\s*(.+)$", Pattern.MULTILINE);
    private static final Pattern REQUIREMENT_PATTERN = Pattern.compile("提交要求[：:]\\s*(.+)");
    private static final Pattern RUBRIC_PATTERN = Pattern.compile("评价标准[：:]\\s*(.+)");

    private final DocumentTextExtractor textExtractor;

    public TaskDocumentParserService(DocumentTextExtractor textExtractor) {
        this.textExtractor = textExtractor;
    }

    public ParsedTaskDocument parse(String fileName, byte[] bytes) {
        String text = textExtractor.extract(fileName, bytes);
        return parseText(fileName, text);
    }

    public ParsedTaskDocument parseText(String fileName, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("任务文档内容为空，无法解析");
        }
        String normalized = text.replace("\r\n", "\n").trim();
        String title = extractFirstGroup(TITLE_PATTERN, normalized);
        if (title.isBlank()) {
            title = normalized.lines().findFirst().orElse("未命名课堂任务").trim();
        }
        String description = extractFirstGroup(DESC_PATTERN, normalized);
        if (description.isBlank()) {
            description = "由任务文档导入。";
        }
        List<ParsedSubTask> subTasks = parseSubTasks(normalized);
        if (subTasks.isEmpty()) {
            subTasks = fallbackSubTasks(normalized);
        }
        String preview = normalized.length() > 200 ? normalized.substring(0, 200) + "..." : normalized;
        return new ParsedTaskDocument(title, description, subTasks, fileName, preview);
    }

    private List<ParsedSubTask> parseSubTasks(String text) {
        List<SectionBlock> blocks = splitSections(text);
        List<ParsedSubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            SectionBlock block = blocks.get(i);
            String requirements = extractFirstGroup(REQUIREMENT_PATTERN, block.body());
            if (requirements.isBlank()) {
                requirements = "请提交「" + block.title() + "」的成果物文档。";
            }
            String rubric = extractFirstGroup(RUBRIC_PATTERN, block.body());
            subTasks.add(new ParsedSubTask(i + 1, block.title(), requirements, rubric));
        }
        return subTasks;
    }

    private List<SectionBlock> splitSections(String text) {
        Matcher matcher = SECTION_PATTERN.matcher(text);
        List<SectionBlock> blocks = new ArrayList<>();
        List<SectionMatch> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new SectionMatch(matcher.start(), matcher.end(), matcher.group(1).trim()));
        }
        for (int i = 0; i < matches.size(); i++) {
            SectionMatch current = matches.get(i);
            int bodyEnd = i + 1 < matches.size() ? matches.get(i + 1).start() : text.length();
            blocks.add(new SectionBlock(current.title(), text.substring(current.end(), bodyEnd).trim()));
        }
        return blocks;
    }

    private List<ParsedSubTask> fallbackSubTasks(String text) {
        List<ParsedSubTask> subTasks = new ArrayList<>();
        int order = 1;
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("子任务") && trimmed.contains("：")) {
                String t = trimmed.substring(trimmed.indexOf('：') + 1).trim();
                subTasks.add(new ParsedSubTask(order++, t, "请提交「" + t + "」的成果物。", ""));
            }
        }
        return subTasks;
    }

    private String extractFirstGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private record SectionBlock(String title, String body) {
    }

    private record SectionMatch(int start, int end, String title) {
    }
}
