package com.neusoft.edu.neullmdev.service.reminder.internal;

import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderResolvedFields;

/**
 * 提醒字段解析结果：成功得到结构化字段，或失败返回可直接下发的 JSON 字符串。
 */
public sealed interface ReminderResolveOutcome permits ReminderResolveOutcome.Ok, ReminderResolveOutcome.Err {

    record Ok(ReminderResolvedFields fields) implements ReminderResolveOutcome {
    }

    /** jsonPayload 为前端/SSE 可用的完整 JSON 文本 */
    record Err(String jsonPayload) implements ReminderResolveOutcome {
    }
}
