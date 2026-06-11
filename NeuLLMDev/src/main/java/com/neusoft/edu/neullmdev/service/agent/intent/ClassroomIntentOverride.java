package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomSubmissionService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 教师端：当模型未输出工具 JSON 时，根据关键词纠偏为课堂 MCP 调用。
 */
@Component
public class ClassroomIntentOverride {

    private static final Pattern TASK_ID = Pattern.compile("TASK-\\d{3}", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBMIT_ID = Pattern.compile("SUBMIT-\\d{3}", Pattern.CASE_INSENSITIVE);

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

    private final ClassroomTaskService taskService;
    private final ClassroomSubmissionService submissionService;

    public ClassroomIntentOverride(ClassroomTaskService taskService,
                                   ClassroomSubmissionService submissionService) {
        this.taskService = taskService;
        this.submissionService = submissionService;
    }

    public FunctionCall reconcile(String userInput, FunctionCall parsed, boolean teacherMode) {
        return reconcile(userInput, parsed, teacherMode, null);
    }

    public FunctionCall reconcile(String userInput, FunctionCall parsed, boolean teacherMode, AuthUser authUser) {
        if (!teacherMode || userInput == null || userInput.isBlank()) {
            return parsed;
        }
        if (!isTeacherSession(authUser)) {
            return parsed;
        }
        if (parsed != null && parsed.getName() != null && CLASSROOM_TOOLS.contains(normalize(parsed.getName()))) {
            return parsed;
        }

        Optional<FunctionCall> forced = tryForce(userInput, authUser);
        if (forced.isEmpty()) {
            return parsed;
        }
        if (parsed == null || parsed.getName() == null || parsed.getName().isBlank()) {
            return forced.get();
        }
        String n = normalize(parsed.getName());
        if (CLASSROOM_TOOLS.contains(n)) {
            return parsed;
        }
        if (isStudentOnlyTool(n)) {
            return forced.get();
        }
        return parsed;
    }

    private Optional<FunctionCall> tryForce(String input, AuthUser authUser) {
        Matcher sm = SUBMIT_ID.matcher(input);
        if (sm.find() && (input.contains("批改") || input.contains("评语") || input.contains("审阅") || input.contains("评分建议"))) {
            return Optional.of(call("assist_grade_submission", Map.of("submissionId", sm.group().toUpperCase())));
        }
        if ((input.contains("最新") || input.contains("最近")) && (input.contains("批改") || input.contains("提交"))) {
            Optional<String> latestSubmit = latestSubmissionId(authUser);
            if (latestSubmit.isPresent()) {
                return Optional.of(call("assist_grade_submission", Map.of("submissionId", latestSubmit.get())));
            }
        }
        if (input.contains("催交") || input.contains("提醒提交") || input.contains("未交")) {
            boolean sendNow = shouldSendRemindersNow(input);
            String tool = sendNow ? "send_unsubmitted_reminders" : "remind_unsubmitted_students";
            return taskIdFromText(input, authUser).map(id -> call(tool, Map.of("taskId", id)));
        }
        if (input.contains("批量批改") || input.contains("批量生成批改")) {
            return taskIdFromText(input, authUser).map(id -> call("batch_assist_grade_submissions", Map.of("taskId", id)));
        }
        if (input.contains("学情") || input.contains("提交进度") || input.contains("完成情况") || input.contains("进度看板")) {
            return taskIdFromText(input, authUser).map(id -> call("build_classroom_dashboard", Map.of("taskId", id)));
        }
        if (input.contains("查看提交") || input.contains("全部提交")) {
            return taskIdFromText(input, authUser).map(id -> call("list_task_submissions", Map.of("taskId", id)));
        }
        if (input.contains("学生名单") || input.contains("本班学生") || input.contains("列出学生")) {
            return Optional.of(call("list_classroom_students", Map.of()));
        }
        Matcher tm = TASK_ID.matcher(input);
        if (tm.find() && (input.contains("发布任务") || input.contains("发布作业") || input.matches(".*发布\\s*TASK.*"))) {
            return Optional.of(call("publish_classroom_task", Map.of("taskId", tm.group().toUpperCase())));
        }
        if (looksLikeCreateTask(input)) {
            return Optional.of(buildCreateTask(input));
        }
        return Optional.empty();
    }

    private boolean looksLikeCreateTask(String input) {
        return input.contains("创建") && (input.contains("作业") || input.contains("任务"))
                || input.contains("布置作业") || input.contains("布置任务")
                || input.contains("设计") && (input.contains("课堂作业") || input.contains("作业"))
                || input.contains("发布作业") && !TASK_ID.matcher(input).find();
    }

    private FunctionCall buildCreateTask(String input) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", extractTitle(input));
        params.put("description", "由教学 Agent 根据教师需求创建。");
        boolean publish = input.contains("直接发布") || input.contains("并发布")
                || (input.contains("发布") && !input.contains("发布作业模板"));
        params.put("publish", publish);
        params.put("subTasks", parseSubTasks(input));
        return call("create_classroom_task", params);
    }

    private static FunctionCall call(String name, Map<String, Object> params) {
        FunctionCall fc = new FunctionCall();
        fc.setName(name);
        fc.setParams(params);
        return fc;
    }

    private String extractTitle(String input) {
        Matcher q = Pattern.compile("[「《]([^》」]+)[》」]").matcher(input);
        if (q.find()) {
            return q.group(1).trim();
        }
        if (input.contains("自主决策")) {
            return "自主决策 Agent 课堂作业";
        }
        return "课堂作业";
    }

    private List<Map<String, String>> parseSubTasks(String input) {
        List<Map<String, String>> items = new ArrayList<>();
        Matcher enumerated = Pattern.compile("(?:子任务|[:：])\\s*([^，,；;。\\n]+(?:、[^，,；;。\\n]+)+)").matcher(input);
        if (enumerated.find()) {
            String part = enumerated.group(1);
            for (String seg : part.split("[、,，]")) {
                String t = seg.trim();
                if (!t.isEmpty()) {
                    items.add(subTask(t, "请按要求完成并提交。"));
                }
            }
        }
        if (items.isEmpty()) {
            int n = 3;
            Matcher num = Pattern.compile("(\\d+)\\s*个\\s*子任务").matcher(input);
            if (num.find()) {
                try {
                    n = Math.min(6, Math.max(1, Integer.parseInt(num.group(1))));
                } catch (NumberFormatException ignored) {
                    n = 3;
                }
            }
            String[] defaults = {"需求分析", "方案设计", "成果交付", "阶段汇报", "总结反思", "答辩准备"};
            for (int i = 0; i < n; i++) {
                String title = i < defaults.length ? defaults[i] : "子任务 " + (i + 1);
                items.add(subTask(title, "请提交「" + title + "」相关成果。"));
            }
        }
        return items;
    }

    private static Map<String, String> subTask(String title, String desc) {
        Map<String, String> m = new HashMap<>();
        m.put("title", title);
        m.put("description", desc);
        return m;
    }

    private Optional<String> taskIdFromText(String input, AuthUser authUser) {
        Matcher m = TASK_ID.matcher(input);
        if (m.find()) {
            return Optional.of(m.group().toUpperCase());
        }
        return latestTaskId(authUser);
    }

    private Optional<String> latestSubmissionId(AuthUser authUser) {
        return latestTaskId(authUser).flatMap(taskId -> AuthContext.callWith(authUser, () -> {
            try {
                List<TaskSubmissionResponse> subs = submissionService.listSubmissions(taskId);
                for (TaskSubmissionResponse s : subs) {
                    String id = s.submissionId();
                    if (id != null) {
                        return Optional.of(id);
                    }
                }
            } catch (Exception ignored) {
                // ignore
            }
            return Optional.empty();
        }));
    }

    private Optional<String> latestTaskId(AuthUser authUser) {
        return AuthContext.callWith(authUser, () -> {
            try {
                List<ClassroomTaskResponse> tasks = taskService.listTasksForTeacher();
                String published = null;
                String any = null;
                for (ClassroomTaskResponse t : tasks) {
                    String id = t.taskId();
                    any = id;
                    if (t.published()) {
                        published = id;
                    }
                }
                if (published != null) {
                    return Optional.of(published);
                }
                return any != null ? Optional.of(any) : Optional.empty();
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    private boolean isTeacherSession(AuthUser authUser) {
        if (authUser != null) {
            return authUser.role() == UserRole.TEACHER;
        }
        try {
            return AuthContext.get() != null && AuthContext.require().role() == UserRole.TEACHER;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isStudentOnlyTool(String name) {
        return Set.of(
                "send_email", "create_travel_reminder", "plan_itinerary", "hotel_recommend",
                "food_search", "get_current_weather", "save_user_profile", "list_my_assignments",
                "submit_assignment"
        ).contains(name);
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    /** 默认直接发邮件；仅当明确要「预览/话术/名单」且未要求发送时走预览工具。 */
    private static boolean shouldSendRemindersNow(String input) {
        if (input.contains("直接") || input.contains("帮我催") || input.contains("发送邮件")
                || input.contains("发邮件") || input.contains("一键催") || input.contains("一键发送")) {
            return true;
        }
        boolean previewOnly = (input.contains("话术") || input.contains("名单") || input.contains("预览"))
                && !input.contains("发送") && !input.contains("邮件");
        return !previewOnly;
    }
}
