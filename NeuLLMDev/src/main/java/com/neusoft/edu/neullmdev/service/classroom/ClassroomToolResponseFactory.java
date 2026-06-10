package com.neusoft.edu.neullmdev.service.classroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/** 课堂工具结果 → 前端 displayKind 卡片 JSON。 */
public final class ClassroomToolResponseFactory {

    private static final Set<String> CLASSROOM_TOOLS = Set.of(
            "create_classroom_task",
            "publish_classroom_task",
            "build_classroom_dashboard",
            "list_task_submissions",
            "list_classroom_students",
            "assist_grade_submission",
            "grade_submission",
            "reject_submission",
            "remind_unsubmitted_students",
            "send_unsubmitted_reminders",
            "batch_assist_grade_submissions"
    );

    private static final Set<String> PREVIEW_TOOLS = Set.of(
            "create_classroom_task",
            "publish_classroom_task"
    );

    private ClassroomToolResponseFactory() {
    }

    public static boolean isClassroomTool(String toolName) {
        return toolName != null && CLASSROOM_TOOLS.contains(toolName.trim().toLowerCase());
    }

    public static boolean needsTeacherPreview(String toolName) {
        return toolName != null && PREVIEW_TOOLS.contains(toolName.trim().toLowerCase());
    }

    public static String createTaskPreview(Map<String, Object> params, ObjectMapper mapper) {
        JSONObject out = new JSONObject();
        out.put("displayKind", "classroom_task_preview");
        out.put("functionName", "create_classroom_task");
        out.put("summary", "请确认作业内容后再发布到班级");
        out.put("preview", paramsToPreview(params, mapper));
        return out.toString();
    }

    public static String publishTaskPreview(String taskId, String title) {
        JSONObject preview = new JSONObject();
        preview.put("taskId", taskId);
        preview.put("title", title != null ? title : taskId);
        JSONObject out = new JSONObject();
        out.put("displayKind", "classroom_publish_preview");
        out.put("functionName", "publish_classroom_task");
        out.put("summary", "确认后将向全班学生发布此作业");
        out.put("preview", preview);
        return out.toString();
    }

    private static JSONObject paramsToPreview(Map<String, Object> params, ObjectMapper mapper) {
        JSONObject p = new JSONObject();
        if (params == null) {
            return p;
        }
        p.put("title", String.valueOf(params.getOrDefault("title", "课堂作业")));
        p.put("description", String.valueOf(params.getOrDefault("description", "")));
        Object pub = params.get("publish");
        boolean publish = pub instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(pub));
        p.put("publish", publish);
        Object sub = params.get("subTasks");
        if (sub != null) {
            try {
                p.put("subTasks", new JSONArray(mapper.writeValueAsString(sub)));
            } catch (Exception e) {
                p.put("subTasks", new JSONArray());
            }
        }
        return p;
    }

    public static String toFrontendJson(ToolResult result, ObjectMapper mapper) {
        if (result == null) {
            return errorJson("工具无返回");
        }
        String tool = result.getToolName() != null ? result.getToolName() : "";
        JSONObject out = new JSONObject();
        out.put("displayKind", displayKind(tool));
        out.put("functionName", tool);
        out.put("summary", result.getSummary() != null ? result.getSummary() : "");
        Object data = result.getData();
        if (data != null) {
            try {
                String json = mapper.writeValueAsString(data);
                if (json.trim().startsWith("[")) {
                    out.put("data", new JSONArray(json));
                } else {
                    out.put("data", new JSONObject(json));
                }
            } catch (Exception e) {
                out.put("data", String.valueOf(data));
            }
        }
        if (data instanceof Map<?, ?> map && map.containsKey("error")) {
            out.put("displayKind", "classroom_error");
            out.put("message", String.valueOf(map.get("error")));
        }
        return out.toString();
    }

    public static String errorJson(String message) {
        return new JSONObject()
                .put("displayKind", "classroom_error")
                .put("message", message != null ? message : "操作失败")
                .toString();
    }

    private static String displayKind(String toolName) {
        if (toolName == null) {
            return "classroom_result";
        }
        return switch (toolName.trim().toLowerCase()) {
            case "create_classroom_task" -> "classroom_task_created";
            case "publish_classroom_task" -> "classroom_task_published";
            case "build_classroom_dashboard" -> "classroom_dashboard";
            case "list_task_submissions" -> "classroom_submissions";
            case "list_classroom_students" -> "classroom_students";
            case "assist_grade_submission" -> "classroom_grading_assist";
            case "grade_submission" -> "classroom_graded";
            case "reject_submission" -> "classroom_rejected";
            case "remind_unsubmitted_students" -> "classroom_remind";
            case "send_unsubmitted_reminders" -> "classroom_remind_sent";
            case "batch_assist_grade_submissions" -> "classroom_batch_grade";
            default -> "classroom_result";
        };
    }
}
