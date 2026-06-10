package com.neusoft.edu.neullmdev.service.reminder.internal;

/**
 * 已从用户输入与工具参数中解析出的提醒字段（领域模型，供持久化与前端展示）。
 */
public record ReminderResolvedFields(
        String eventName,
        String eventDate,
        String eventTime,
        String phoneNumber,
        String email,
        String description,
        String notifyMethod
) {
}
