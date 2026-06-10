package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomTaskMapper;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师批改辅助：调用 Kimi 总结提交内容并给出分数/评语建议（供 REST 与 MCP 工具复用）。
 */
@Slf4j
@Service
public class SubmissionGradingAssistService {

    private static final int MAX_CONTENT_CHARS = 12_000;
    private static final String SYSTEM_PROMPT = """
            你是「智学伴」课堂批改助手，帮助教师快速理解学生作业并给出可采纳的批改建议。
            你必须仅根据提供的任务说明、子任务要求与学生提交正文进行分析，不要编造未出现的内容。
            输出必须是单个 JSON 对象，不要 markdown 代码块，不要额外说明。字段如下：
            {
              "summary": "200字以内作业摘要",
              "completionLevel": "complete|partial|insufficient",
              "suggestedScore": 0-100 的整数,
              "suggestedComment": "可直接发给学生的评语，80字以内",
              "strengths": ["优点1", "优点2"],
              "issues": ["待改进1"],
              "recommendation": "approve|revise|needs_review",
              "teacherTip": "给教师的一句话操作建议"
            }
            recommendation 含义：approve=建议通过并给分；revise=建议打回修改；needs_review=需教师本人细读原文。
            """;

    private final KimiChatService kimiChatService;
    private final ClassroomService classroomService;
    private final ClassroomDashboardService dashboardService;
    private final ClassroomSubmissionMapper submissionMapper;
    private final ClassroomTaskMapper taskMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final SysUserMapper sysUserMapper;

    public SubmissionGradingAssistService(KimiChatService kimiChatService,
                                          ClassroomService classroomService,
                                          ClassroomDashboardService dashboardService,
                                          ClassroomSubmissionMapper submissionMapper,
                                          ClassroomTaskMapper taskMapper,
                                          ClassroomSubTaskMapper subTaskMapper,
                                          SysUserMapper sysUserMapper) {
        this.kimiChatService = kimiChatService;
        this.classroomService = classroomService;
        this.dashboardService = dashboardService;
        this.submissionMapper = submissionMapper;
        this.taskMapper = taskMapper;
        this.subTaskMapper = subTaskMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public Map<String, Object> assist(String submissionId) {
        classroomService.requireTeacher();
        ClassroomSubmissionEntity sub = submissionMapper.findById(submissionId);
        if (sub == null) {
            throw new IllegalArgumentException("提交记录不存在：" + submissionId);
        }
        ClassroomTaskEntity task = taskMapper.findById(sub.getTaskId());
        ClassroomSubTaskEntity subTask = subTaskMapper.findOne(sub.getTaskId(), sub.getSubTaskId());
        if (task == null || subTask == null) {
            throw new IllegalArgumentException("任务或子任务不存在");
        }

        var student = sysUserMapper.findById(sub.getStudentUserId());
        String studentName = student != null ? student.getDisplayName() : "学生";
        String studentNo = student != null ? student.getStudentNo() : "";

        String userPrompt = buildUserPrompt(task, subTask, sub, studentName, studentNo);
        String raw;
        try {
            raw = kimiChatService.chatWithSystem(SYSTEM_PROMPT, userPrompt, 0.25)
                    .block(Duration.ofSeconds(120));
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Timeout")) {
                throw new IllegalStateException("AI 响应超时（海外服务器访问百炼较慢），请稍后重试");
            }
            throw e;
        }
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("AI 未返回结果，请稍后重试");
        }
        if (raw.startsWith("解析响应出错") || raw.contains("请稍后重试")) {
            throw new IllegalStateException(raw);
        }

        Map<String, Object> parsed = parseAssistJson(raw);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submissionId", submissionId);
        result.put("taskId", sub.getTaskId());
        result.put("subTaskId", sub.getSubTaskId());
        result.put("studentName", studentName);
        result.put("studentNo", studentNo);
        result.putAll(parsed);
        result.put("modelNote", "建议仅供参考，采纳后请教师确认保存。");
        return result;
    }

    private String buildUserPrompt(ClassroomTaskEntity task,
                                   ClassroomSubTaskEntity subTask,
                                   ClassroomSubmissionEntity sub,
                                   String studentName,
                                   String studentNo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【课堂任务】").append(task.getTitle()).append('\n');
        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            sb.append("任务说明：").append(task.getDescription()).append('\n');
        }
        sb.append("【当前子任务】").append(subTask.getTitle()).append('\n');
        if (subTask.getDescription() != null && !subTask.getDescription().isBlank()) {
            sb.append("子任务要求：").append(subTask.getDescription()).append('\n');
        }
        sb.append("【学生】").append(studentName).append(" (").append(studentNo).append(")\n");
        sb.append("【提交状态】").append(sub.getStatus()).append('\n');
        if (sub.getFileName() != null) {
            sb.append("【文件名】").append(sub.getFileName()).append('\n');
        }

        appendStudentProgress(sb, sub.getTaskId(), studentNo);

        String content = sub.getContent() == null ? "" : sub.getContent().trim();
        if (content.length() > MAX_CONTENT_CHARS) {
            content = content.substring(0, MAX_CONTENT_CHARS) + "\n…（正文已截断，仅分析前 " + MAX_CONTENT_CHARS + " 字）";
        }
        sb.append("\n【学生提交正文】\n").append(content.isEmpty() ? "（无文本内容）" : content);
        return sb.toString();
    }

    private void appendStudentProgress(StringBuilder sb, String taskId, String studentNo) {
        try {
            ProgressDashboard dash = dashboardService.buildDashboard(taskId);
            if (dash.rows() == null) {
                return;
            }
            for (Map<String, Object> row : dash.rows()) {
                if (!studentNo.equals(String.valueOf(row.get("studentId")))) {
                    continue;
                }
                sb.append("【该生本任务进度】");
                List<Map<String, Object>> cells = (List<Map<String, Object>>) row.get("cells");
                if (cells != null) {
                    for (Map<String, Object> cell : cells) {
                        sb.append(' ')
                                .append(cell.getOrDefault("label", cell.get("state")))
                                .append(';');
                    }
                }
                sb.append('\n');
                break;
            }
        } catch (Exception e) {
            log.debug("学情上下文加载跳过: {}", e.getMessage());
        }
    }

    private Map<String, Object> parseAssistJson(String raw) {
        try {
            JSONObject json = extractJsonObject(raw);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("summary", json.optString("summary", ""));
            map.put("completionLevel", json.optString("completionLevel", "needs_review"));
            map.put("suggestedScore", clampScore(json.optInt("suggestedScore", 0)));
            map.put("suggestedComment", json.optString("suggestedComment", ""));
            map.put("strengths", jsonArrayToList(json.optJSONArray("strengths")));
            map.put("issues", jsonArrayToList(json.optJSONArray("issues")));
            map.put("recommendation", json.optString("recommendation", "needs_review"));
            map.put("teacherTip", json.optString("teacherTip", ""));
            return map;
        } catch (Exception e) {
            log.warn("批改辅助 JSON 解析失败: {}", e.getMessage());
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("summary", raw.length() > 500 ? raw.substring(0, 500) + "…" : raw);
            fallback.put("completionLevel", "needs_review");
            fallback.put("suggestedScore", null);
            fallback.put("suggestedComment", "");
            fallback.put("strengths", List.of());
            fallback.put("issues", List.of("AI 返回格式异常，请教师阅读原文"));
            fallback.put("recommendation", "needs_review");
            fallback.put("teacherTip", "请重新点击 AI 审阅或自行批改");
            return fallback;
        }
    }

    private static JSONObject extractJsonObject(String raw) {
        String s = raw.trim();
        if (s.contains("```")) {
            int jsonFence = s.indexOf("```json");
            int fence = jsonFence >= 0 ? jsonFence : s.indexOf("```");
            int start = s.indexOf('\n', fence);
            if (start < 0) {
                start = fence + 3;
            } else {
                start++;
            }
            int end = s.indexOf("```", start);
            if (end > start) {
                s = s.substring(start, end).trim();
            }
        }
        int brace = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (brace >= 0 && last > brace) {
            s = s.substring(brace, last + 1);
        }
        return new JSONObject(s);
    }

    private static List<String> jsonArrayToList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr == null) {
            return list;
        }
        for (int i = 0; i < arr.length(); i++) {
            String item = arr.optString(i, "").trim();
            if (!item.isEmpty()) {
                list.add(item);
            }
        }
        return list;
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
