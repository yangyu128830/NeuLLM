package com.neusoft.edu.neullmdev.service.reminder.internal;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 旅行提醒工具返回给前端的结构化 JSON（与 ChatView 中 displayKind 约定一致）。
 */
@Component
public class ReminderToolResponseFactory {

    private static final DateTimeFormatter LABEL_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    public String missingDateErrorJson() {
        return errorJson("请告诉我哪一天提醒（可以说「今天」「明天」或具体日期）～");
    }

    public String parseErrorJson() {
        return errorJson("日期时间好像有点对不上，试试用「今天 15:00」或「2026-05-11 15:00」这种格式～");
    }

    public String errorJson(String friendlyMessage) {
        return new JSONObject()
                .put("functionName", "create_travel_reminder")
                .put("displayKind", "reminder_error")
                .put("message", friendlyMessage)
                .toString();
    }

    public String previewFormJson(ReminderResolvedFields fields, int advanceMinutes) {
        JSONObject json = new JSONObject();
        json.put("functionName", "create_travel_reminder");
        json.put("title", safe(fields.eventName()));
        json.put("location", "");
        json.put("datetime", toDatetimeLocal(fields.eventDate(), fields.eventTime()));
        json.put("notes", safe(fields.description()));
        json.put("advanceNotice", String.valueOf(advanceMinutes));
        if (fields.email() != null && !fields.email().trim().isEmpty()) {
            json.put("email", fields.email().trim());
        }
        if (fields.phoneNumber() != null && !fields.phoneNumber().trim().isEmpty()) {
            json.put("phoneNumber", fields.phoneNumber().trim());
        }
        return json.toString();
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private static String toDatetimeLocal(String eventDate, String eventTime) {
        if (eventDate == null || eventDate.isBlank()) {
            return "";
        }
        String date = eventDate.trim();
        String time = (eventTime != null && !eventTime.isBlank()) ? eventTime.trim() : "09:00";
        if (time.length() == 5) {
            return date + "T" + time;
        }
        if (time.length() >= 8) {
            return date + "T" + time.substring(0, 5);
        }
        return date + "T09:00";
    }

    public String successJson(
            ReminderResolvedFields fields,
            LocalDateTime eventDateTime,
            LocalDateTime reminderDateTime,
            int advanceMinutes,
            boolean persistedOk,
            boolean emailSentNow,
            String emailAck,
            String hint) {

        JSONObject json = new JSONObject();
        json.put("functionName", "create_travel_reminder");
        json.put("displayKind", "reminder_saved");
        json.put("title", fields.eventName());
        json.put("eventDate", fields.eventDate());
        json.put("eventTime", fields.eventTime());
        json.put("eventDateTimeLabel", eventDateTime.format(LABEL_FMT));
        json.put("reminderAtLabel", reminderDateTime.format(LABEL_FMT));
        json.put("advanceMinutes", advanceMinutes);
        json.put("persisted", persistedOk);
        json.put("emailNotifyConfigured", fields.email() != null && !fields.email().trim().isEmpty());
        json.put("confirmationEmailSent", emailSentNow);
        json.put("hint", hint);
        if (fields.description() != null && !fields.description().trim().isEmpty()) {
            json.put("notes", fields.description());
        }
        if (emailSentNow && emailAck != null && !emailAck.isBlank()) {
            json.put("emailResultBrief", emailAck.length() > 120 ? emailAck.substring(0, 120) + "…" : emailAck);
        }
        return json.toString();
    }
}
