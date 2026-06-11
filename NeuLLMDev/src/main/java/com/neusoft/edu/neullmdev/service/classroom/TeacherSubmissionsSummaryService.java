package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TeacherSubmissionsSummaryService {

    private static final String SYSTEM_PROMPT = """
            你是「教学 Agent」，帮教师用温暖、专业的口语总结学生作业提交情况。
            根据 JSON 数据写 2～4 句话：
            ● 先说有没有待批改（pendingCount）、涉及哪些作业/学生。
            ● 若 pendingCount 为 0 且 totalSubmissions 大于 0：说明最近暂无新的待批改提交，可去批改台看历史。
            ● 若 totalSubmissions 为 0：友好说明还没有学生提交。
            ● 不要 markdown、不要编号；严禁编造数据里没有的 submissionId 或学生名。
            """;

    private final ClassroomAccessSupport access;
    private final ClassroomTaskService taskService;
    private final ClassroomSubmissionService submissionService;
    private final KimiChatService kimiChatService;

    public TeacherSubmissionsSummaryService(ClassroomAccessSupport access,
                                            ClassroomTaskService taskService,
                                            ClassroomSubmissionService submissionService,
                                            KimiChatService kimiChatService) {
        this.access = access;
        this.taskService = taskService;
        this.submissionService = submissionService;
        this.kimiChatService = kimiChatService;
    }

    public Map<String, Object> summarizeForCurrentTeacher() {
        access.requireTeacher();
        List<ClassroomTaskResponse> tasks = taskService.listTasksForTeacher();

        List<Map<String, Object>> all = new ArrayList<>();
        for (ClassroomTaskResponse task : tasks) {
            if (!task.published()) {
                continue;
            }
            String taskTitle = task.title() != null ? task.title() : task.taskId();
            for (TaskSubmissionResponse sub : submissionService.listSubmissions(task.taskId())) {
                Map<String, Object> row = submissionToMap(sub);
                row.put("taskTitle", taskTitle);
                row.put("statusLabel", statusLabel(sub.status()));
                all.add(row);
            }
        }

        all.sort(Comparator.comparing(
                TeacherSubmissionsSummaryService::submittedAtKey,
                Comparator.nullsLast(Comparator.reverseOrder())));

        List<Map<String, Object>> pendingItems = all.stream()
                .filter(s -> SubmissionStatus.SUBMITTED.name().equals(String.valueOf(s.get("status"))))
                .toList();

        String summary = composeSummary(all.size(), pendingItems.size(), pendingItems, tasks.size());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("totalSubmissions", all.size());
        out.put("pendingCount", pendingItems.size());
        out.put("pendingItems", pendingItems);
        out.put("publishedTaskCount", tasks.stream().filter(ClassroomTaskResponse::published).count());
        out.put("empty", all.isEmpty());
        out.put("allCaughtUp", !all.isEmpty() && pendingItems.isEmpty());
        return out;
    }

    private static Map<String, Object> submissionToMap(TaskSubmissionResponse sub) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("submissionId", sub.submissionId());
        row.put("taskId", sub.taskId());
        row.put("subTaskId", sub.subTaskId());
        row.put("studentUserId", sub.studentUserId());
        row.put("fileName", sub.fileName());
        row.put("content", sub.content());
        row.put("status", sub.status());
        row.put("score", sub.score());
        row.put("teacherComment", sub.teacherComment());
        row.put("submittedAt", sub.submittedAt());
        row.put("gradedAt", sub.gradedAt());
        row.put("studentName", sub.studentName());
        row.put("studentNo", sub.studentNo());
        return row;
    }

    private static LocalDateTime submittedAtKey(Map<String, Object> sub) {
        Object v = sub.get("submittedAt");
        if (v instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (v == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(v).replace(' ', 'T').substring(0, 19));
        } catch (Exception e) {
            return null;
        }
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "SUBMITTED" -> "待批改";
            case "GRADED" -> "已批改";
            case "REJECTED" -> "需重交";
            default -> status;
        };
    }

    private String composeSummary(int total, int pending, List<Map<String, Object>> pendingItems, int taskCount) {
        if (total == 0) {
            return fallbackEmpty(taskCount);
        }
        try {
            JSONObject payload = new JSONObject()
                    .put("totalSubmissions", total)
                    .put("pendingCount", pending)
                    .put("publishedTaskCount", taskCount);
            JSONArray arr = new JSONArray();
            for (Map<String, Object> item : pendingItems.stream().limit(8).toList()) {
                arr.put(new JSONObject()
                        .put("submissionId", item.getOrDefault("submissionId", ""))
                        .put("studentName", item.getOrDefault("studentName", ""))
                        .put("taskTitle", item.getOrDefault("taskTitle", ""))
                        .put("statusLabel", item.getOrDefault("statusLabel", ""))
                        .put("submittedAt", String.valueOf(item.getOrDefault("submittedAt", ""))));
            }
            payload.put("pendingItems", arr);

            String raw = kimiChatService.chatWithSystem(SYSTEM_PROMPT, payload.toString(), 0.4)
                    .block(Duration.ofSeconds(45));
            if (raw != null && !raw.isBlank() && !raw.startsWith("解析响应出错")) {
                return raw.trim();
            }
        } catch (Exception e) {
            log.warn("教师提交 AI 总结失败，使用规则兜底: {}", e.getMessage());
        }
        return fallbackSummary(total, pending, pendingItems);
    }

    private static String fallbackEmpty(int taskCount) {
        if (taskCount == 0) {
            return "目前还没有已发布的课堂作业～发布任务后，学生提交会在这里汇总。";
        }
        return "已发布作业里还没有学生提交记录。学生交作业后，待批改的会出现在这里。";
    }

    private static String fallbackSummary(int total, int pending, List<Map<String, Object>> pendingItems) {
        if (pending == 0) {
            return "最近暂无待批改的新提交（共 " + total + " 条历史提交）。可以去批改台查看已批改记录。";
        }
        Map<String, Object> first = pendingItems.get(0);
        return "有 " + pending + " 条待批改提交（共 " + total + " 条）。建议优先看 "
                + first.getOrDefault("studentName", "学生") + " 的「"
                + first.getOrDefault("taskTitle", "作业") + "」。";
    }
}
