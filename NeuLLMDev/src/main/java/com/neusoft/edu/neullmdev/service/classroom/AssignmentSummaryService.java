package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.response.StudentAssignmentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentSubTaskResponse;
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

@Slf4j
@Service
public class AssignmentSummaryService {

    private static final String SYSTEM_PROMPT = """
            你是「智学伴」，帮学生用温暖、口语的方式总结作业待办情况。
            根据 JSON 数据写 2～4 句话，像朋友发微信：
            ● 说明还有多少步骤待提交、有没有需重交的、是否全部完成。
            ● 若有 endTime 临近的可轻轻提醒别拖到最后。
            ● 若 pendingCount 为 0 且 totalTasks 大于 0：肯定用户「都交齐啦」。
            ● 若 totalTasks 为 0：说明暂无已发布作业。
            ● 不要 markdown、不要编号列表；严禁编造数据里没有的作业名。
            """;

    private final ClassroomTaskService taskService;
    private final KimiChatService kimiChatService;

    public AssignmentSummaryService(ClassroomTaskService taskService, KimiChatService kimiChatService) {
        this.taskService = taskService;
        this.kimiChatService = kimiChatService;
    }

    public Map<String, Object> summarizeForCurrentStudent() {
        List<StudentAssignmentResponse> tasks = taskService.listMyAssignments();
        List<Map<String, Object>> pendingItems = collectPendingItems(tasks);

        int completeTasks = 0;
        for (StudentAssignmentResponse task : tasks) {
            if (isTaskComplete(task)) {
                completeTasks++;
            }
        }

        String summary = composeSummary(tasks.size(), completeTasks, pendingItems);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("totalTasks", tasks.size());
        out.put("completeTasks", completeTasks);
        out.put("pendingCount", pendingItems.size());
        out.put("pendingItems", pendingItems);
        out.put("empty", tasks.isEmpty());
        out.put("allDone", !tasks.isEmpty() && pendingItems.isEmpty());
        return out;
    }

    private static List<Map<String, Object>> collectPendingItems(List<StudentAssignmentResponse> tasks) {
        List<Map<String, Object>> pending = new ArrayList<>();
        for (StudentAssignmentResponse task : tasks) {
            if (isTaskComplete(task)) {
                continue;
            }
            for (StudentSubTaskResponse sub : task.subTasks()) {
                if (!isPendingSubTask(sub)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("taskId", task.taskId());
                item.put("taskTitle", task.title());
                item.put("subTaskId", sub.subTaskId());
                item.put("subTaskTitle", sub.title());
                item.put("statusLabel", sub.statusLabel());
                item.put("status", sub.status());
                if (task.endTime() != null) {
                    item.put("endTime", task.endTime());
                }
                pending.add(item);
            }
        }
        return pending;
    }

    private static boolean isTaskComplete(StudentAssignmentResponse task) {
        if (task.subTasks() == null || task.subTasks().isEmpty()) {
            return false;
        }
        for (StudentSubTaskResponse sub : task.subTasks()) {
            if (!isSubmittedSubTask(sub)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSubmittedSubTask(StudentSubTaskResponse sub) {
        return sub.submissionId() != null && !sub.submissionId().isBlank() && !"REJECTED".equals(sub.status());
    }

    private static boolean isPendingSubTask(StudentSubTaskResponse sub) {
        if (isSubmittedSubTask(sub)) {
            return false;
        }
        if ("REJECTED".equals(sub.status())) {
            return true;
        }
        return "AVAILABLE".equals(sub.status()) && sub.canSubmit();
    }

    private String composeSummary(int totalTasks, int completeTasks, List<Map<String, Object>> pendingItems) {
        if (totalTasks == 0) {
            return fallbackEmpty();
        }
        try {
            JSONObject payload = new JSONObject()
                    .put("totalTasks", totalTasks)
                    .put("completeTasks", completeTasks)
                    .put("pendingCount", pendingItems.size());
            JSONArray arr = new JSONArray();
            for (Map<String, Object> item : pendingItems.stream().limit(10).toList()) {
                arr.put(new JSONObject(item));
            }
            payload.put("pendingItems", arr);

            String raw = kimiChatService.chatWithSystem(SYSTEM_PROMPT, payload.toString(), 0.4)
                    .block(Duration.ofSeconds(45));
            if (raw != null && !raw.isBlank() && !raw.startsWith("解析响应出错")) {
                return raw.trim();
            }
        } catch (Exception e) {
            log.warn("作业 AI 总结失败，使用规则兜底: {}", e.getMessage());
        }
        return fallbackSummary(totalTasks, completeTasks, pendingItems);
    }

    private static String fallbackEmpty() {
        return "目前还没有已发布的作业～老师布置任务后，会出现在「我的作业」里，到时候我再帮你看还有哪些步骤要交。";
    }

    private static String fallbackSummary(int totalTasks, int completeTasks, List<Map<String, Object>> pendingItems) {
        if (pendingItems.isEmpty()) {
            return "太棒啦，" + totalTasks + " 个作业的步骤你都交齐了～有批改结果会在消息中心通知你。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("你还有 ").append(pendingItems.size()).append(" 个步骤待处理");
        sb.append("（共 ").append(totalTasks).append(" 个作业，已完成 ").append(completeTasks).append(" 个）。");
        Map<String, Object> first = pendingItems.get(0);
        sb.append("建议优先：「").append(first.getOrDefault("taskTitle", "")).append("」里的「")
                .append(first.getOrDefault("subTaskTitle", "")).append("」。");
        return sb.toString();
    }
}
