package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.profile.UserProfileLookupResult;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.model.notification.NotificationType;
import com.neusoft.edu.neullmdev.service.communication.SendEmailService;
import com.neusoft.edu.neullmdev.service.notification.NotificationService;
import com.neusoft.edu.neullmdev.service.profile.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassroomReminderService {

    private final ClassroomAccessSupport access;
    private final ClassroomDashboardService dashboardService;
    private final UserProfileService userProfileService;
    private final SendEmailService sendEmailService;
    private final NotificationService notificationService;

    public ClassroomReminderService(ClassroomAccessSupport access,
                                    ClassroomDashboardService dashboardService,
                                    UserProfileService userProfileService,
                                    SendEmailService sendEmailService,
                                    NotificationService notificationService) {
        this.access = access;
        this.dashboardService = dashboardService;
        this.userProfileService = userProfileService;
        this.sendEmailService = sendEmailService;
        this.notificationService = notificationService;
    }

    public Map<String, Object> buildReminder(String taskId) {
        ProgressDashboard dashboard = dashboardService.buildDashboard(taskId);
        List<Map<String, Object>> pending = new ArrayList<>();
        for (Map<String, Object> row : dashboard.rows()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) row.get("cells");
            boolean hasAvailable = cells.stream().anyMatch(c -> "AVAILABLE".equals(c.get("state")));
            boolean hasRevision = cells.stream().anyMatch(c -> "NEEDS_REVISION".equals(c.get("state")));
            if (hasAvailable || hasRevision) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("studentId", row.get("studentId"));
                item.put("studentUserId", row.get("studentUserId"));
                item.put("studentName", row.get("studentName"));
                item.put("status", hasRevision ? "需重交" : "待提交");
                pending.add(item);
            }
        }
        String reminderMessage = buildMessage(dashboard.title(), pending);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("title", dashboard.title());
        result.put("pendingStudents", pending);
        result.put("pendingCount", pending.size());
        result.put("reminderMessage", reminderMessage);
        result.put("shortMessage", buildShortMessage(dashboard.title(), pending));
        return result;
    }

    public Map<String, Object> sendReminders(String taskId) {
        access.requireTeacher();
        Map<String, Object> built = buildReminder(taskId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pending = (List<Map<String, Object>>) built.get("pendingStudents");
        String title = String.valueOf(built.get("title"));

        List<Map<String, Object>> emailSent = new ArrayList<>();
        List<Map<String, Object>> inAppSent = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        if (pending.isEmpty()) {
            Map<String, Object> out = new LinkedHashMap<>(built);
            out.put("sentCount", 0);
            out.put("inAppCount", 0);
            out.put("skippedCount", 0);
            out.put("failedCount", 0);
            out.put("sent", emailSent);
            out.put("inAppSent", inAppSent);
            out.put("skipped", skipped);
            out.put("failed", failed);
            out.put("allSubmitted", true);
            return out;
        }

        for (Map<String, Object> student : pending) {
            Map<String, Object> delivery = deliverReminder(taskId, title, student, null, null);
            mergeDeliveryLists(delivery, emailSent, inAppSent, skipped, failed);
        }

        Map<String, Object> out = new LinkedHashMap<>(built);
        out.put("sentCount", emailSent.size());
        out.put("inAppCount", inAppSent.size());
        out.put("skippedCount", skipped.size());
        out.put("failedCount", failed.size());
        out.put("sent", emailSent);
        out.put("inAppSent", inAppSent);
        out.put("skipped", skipped);
        out.put("failed", failed);
        out.put("allSubmitted", false);
        return out;
    }

    public Map<String, Object> sendReminderToStudent(String taskId, Long studentUserId, String subTaskId) {
        access.requireTeacher();
        if (studentUserId == null) {
            throw new IllegalArgumentException("studentUserId 不能为空");
        }
        ProgressDashboard dashboard = dashboardService.buildDashboard(taskId);
        Map<String, Object> row = findStudentRow(dashboard, studentUserId);
        if (row == null) {
            throw new IllegalArgumentException("学生不在本任务班级中");
        }

        ReminderTarget target = resolveReminderTarget(row, subTaskId);
        if (!target.remindable()) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("taskId", taskId);
            out.put("title", dashboard.title());
            out.put("studentId", row.get("studentId"));
            out.put("studentUserId", studentUserId);
            out.put("studentName", row.get("studentName"));
            out.put("notRemindable", true);
            out.put("inAppSent", false);
            out.put("emailSent", false);
            return out;
        }

        Map<String, Object> student = new LinkedHashMap<>();
        student.put("studentId", row.get("studentId"));
        student.put("studentUserId", studentUserId);
        student.put("studentName", row.get("studentName"));
        student.put("status", target.status());

        Map<String, Object> delivery = deliverReminder(
                taskId, dashboard.title(), student, target.subTaskTitle(), target.subTaskId());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("title", dashboard.title());
        out.put("studentId", row.get("studentId"));
        out.put("studentUserId", studentUserId);
        out.put("studentName", row.get("studentName"));
        out.put("subTaskId", target.subTaskId());
        out.put("subTaskTitle", target.subTaskTitle());
        out.put("status", target.status());
        out.put("notRemindable", false);
        out.put("inAppSent", delivery.get("inAppSent"));
        out.put("emailSent", delivery.get("emailSent"));
        out.put("skipped", delivery.get("skipped"));
        out.put("failed", delivery.get("failed"));
        if (delivery.get("email") != null) {
            out.put("email", delivery.get("email"));
        }
        if (delivery.get("reason") != null) {
            out.put("reason", delivery.get("reason"));
        }
        return out;
    }

    private Map<String, Object> findStudentRow(ProgressDashboard dashboard, Long studentUserId) {
        for (Map<String, Object> row : dashboard.rows()) {
            Object uidObj = row.get("studentUserId");
            Long uid = uidObj instanceof Number n ? n.longValue() : null;
            if (studentUserId.equals(uid)) {
                return row;
            }
        }
        return null;
    }

    private ReminderTarget resolveReminderTarget(Map<String, Object> row, String subTaskId) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) row.get("cells");
        if (StringUtils.hasText(subTaskId)) {
            for (Map<String, Object> cell : cells) {
                if (!subTaskId.equals(cell.get("subTaskId"))) {
                    continue;
                }
                String state = String.valueOf(cell.get("state"));
                if (!isRemindableState(state)) {
                    return ReminderTarget.notRemindable();
                }
                String title = cell.get("title") != null ? String.valueOf(cell.get("title")) : null;
                String status = "NEEDS_REVISION".equals(state) ? "需重交" : "待提交";
                return new ReminderTarget(true, status, subTaskId, title);
            }
            return ReminderTarget.notRemindable();
        }

        boolean hasRevision = cells.stream().anyMatch(c -> "NEEDS_REVISION".equals(c.get("state")));
        boolean hasAvailable = cells.stream().anyMatch(c -> "AVAILABLE".equals(c.get("state")));
        if (!hasRevision && !hasAvailable) {
            return ReminderTarget.notRemindable();
        }

        Map<String, Object> focusCell = null;
        for (Map<String, Object> cell : cells) {
            String state = String.valueOf(cell.get("state"));
            if ("NEEDS_REVISION".equals(state) || "AVAILABLE".equals(state)) {
                focusCell = cell;
                break;
            }
        }
        String status = hasRevision ? "需重交" : "待提交";
        String focusSubTaskId = focusCell != null ? String.valueOf(focusCell.get("subTaskId")) : null;
        String focusTitle = focusCell != null && focusCell.get("title") != null
                ? String.valueOf(focusCell.get("title")) : null;
        return new ReminderTarget(true, status, focusSubTaskId, focusTitle);
    }

    private boolean isRemindableState(String state) {
        return "AVAILABLE".equals(state) || "NEEDS_REVISION".equals(state);
    }

    private Map<String, Object> deliverReminder(String taskId,
                                                String taskTitle,
                                                Map<String, Object> student,
                                                String subTaskTitle,
                                                String subTaskId) {
        String name = String.valueOf(student.get("studentName"));
        String status = String.valueOf(student.get("status"));
        Object uidObj = student.get("studentUserId");
        Long studentUserId = uidObj instanceof Number n ? n.longValue() : null;
        String subject = "【智学伴】课堂作业催交：" + taskTitle;

        Map<String, Object> result = new LinkedHashMap<>(student);
        result.put("subTaskId", subTaskId);
        result.put("subTaskTitle", subTaskTitle);
        result.put("inAppSent", false);
        result.put("emailSent", false);
        result.put("skipped", false);
        result.put("failed", false);

        if (studentUserId != null) {
            String inAppTitle = "作业催交：" + taskTitle;
            String inAppBody = buildInAppBody(name, taskTitle, status, subTaskTitle);
            notificationService.notifyUser(studentUserId, NotificationType.TASK_REMIND,
                    inAppTitle, inAppBody, "/assignments", taskId);
            result.put("inAppSent", true);
            result.put("channel", "in_app");
        }

        UserProfileLookupResult lookup = userProfileService.lookupByName(name);
        if (!lookup.isOk() || !StringUtils.hasText(lookup.getEmail())) {
            result.put("skipped", true);
            result.put("reason", lookup.isOk() ? "未配置邮箱（站内消息已送达）" : "未找到邮箱资料（站内消息已送达）");
            return result;
        }

        String body = buildPersonalEmail(name, taskTitle, status, subTaskTitle);
        String deliverResult = sendEmailService.deliver(lookup.getEmail().trim(), subject, body);
        result.put("email", lookup.getEmail());
        result.put("deliverResult", deliverResult);
        if (deliverResult != null && deliverResult.contains("成功")) {
            result.put("emailSent", true);
        } else {
            result.put("failed", true);
        }
        return result;
    }

    private void mergeDeliveryLists(Map<String, Object> delivery,
                                    List<Map<String, Object>> emailSent,
                                    List<Map<String, Object>> inAppSent,
                                    List<Map<String, Object>> skipped,
                                    List<Map<String, Object>> failed) {
        if (Boolean.TRUE.equals(delivery.get("inAppSent"))) {
            inAppSent.add(new LinkedHashMap<>(delivery));
        }
        if (Boolean.TRUE.equals(delivery.get("emailSent"))) {
            emailSent.add(new LinkedHashMap<>(delivery));
        } else if (Boolean.TRUE.equals(delivery.get("skipped"))) {
            skipped.add(new LinkedHashMap<>(delivery));
        } else if (Boolean.TRUE.equals(delivery.get("failed"))) {
            failed.add(new LinkedHashMap<>(delivery));
        }
    }

    private String buildInAppBody(String studentName, String taskTitle, String status, String subTaskTitle) {
        if (StringUtils.hasText(subTaskTitle)) {
            return "同学 " + studentName + "，您有一份课堂作业「" + taskTitle + "」的子任务「"
                    + subTaskTitle + "」尚未完成（" + status + "）。请尽快前往「我的作业」提交。";
        }
        return "同学 " + studentName + "，您有一份课堂作业「" + taskTitle + "」尚未完成（" + status
                + "）。请尽快前往「我的作业」提交子任务。";
    }

    private String buildShortMessage(String title, List<Map<String, Object>> pending) {
        if (pending.isEmpty()) {
            return "当前全班已完成提交，无需催交。";
        }
        return "请尚未完成「" + title + "」的 " + pending.size() + " 名同学尽快登录智学伴提交子任务。";
    }

    private String buildPersonalEmail(String studentName, String taskTitle, String status, String subTaskTitle) {
        String focus = StringUtils.hasText(subTaskTitle)
                ? "子任务「" + subTaskTitle + "」"
                : "作业";
        return "同学 " + studentName + "，您好：\n\n"
                + "您有一份课堂作业「" + taskTitle + "」的" + focus + "尚未完成（当前状态：" + status + "）。\n"
                + "请尽快登录智学伴完成子任务提交。\n\n"
                + "如有疑问请联系任课教师。\n"
                + "—— 智学伴教学 Agent";
    }

    private record ReminderTarget(boolean remindable, String status, String subTaskId, String subTaskTitle) {
        static ReminderTarget notRemindable() {
            return new ReminderTarget(false, null, null, null);
        }
    }

    private String buildMessage(String title, List<Map<String, Object>> pending) {
        if (pending.isEmpty()) {
            return "【" + title + "】当前全班已提交，无需催交。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(buildShortMessage(title, pending));
        sb.append("\n\n待跟进名单：\n");
        int n = 0;
        for (Map<String, Object> p : pending) {
            if (n >= 15) {
                sb.append("… 等共 ").append(pending.size()).append(" 人\n");
                break;
            }
            sb.append("· ").append(p.get("studentName")).append("（").append(p.get("studentId")).append("）")
                    .append(" — ").append(p.get("status")).append('\n');
            n++;
        }
        return sb.toString();
    }
}
