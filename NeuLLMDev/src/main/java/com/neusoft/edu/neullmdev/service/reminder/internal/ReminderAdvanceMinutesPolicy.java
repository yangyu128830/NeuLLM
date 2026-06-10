package com.neusoft.edu.neullmdev.service.reminder.internal;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import org.springframework.stereotype.Component;

/**
 * 提前提醒分钟数策略（与模型 advanceNotice 对齐）。
 */
@Component
public class ReminderAdvanceMinutesPolicy {

    private static final int DEFAULT_MINUTES = 15;
    private static final int MAX_MINUTES = 10080; // 7 天

    public int resolve(TravelReminderParams params) {
        if (params.getAdvanceNotice() == null || params.getAdvanceNotice().isBlank()) {
            return DEFAULT_MINUTES;
        }
        try {
            int v = Integer.parseInt(params.getAdvanceNotice().trim());
            if (v >= 0 && v <= MAX_MINUTES) {
                return v;
            }
        } catch (NumberFormatException ignored) {
        }
        return DEFAULT_MINUTES;
    }
}
