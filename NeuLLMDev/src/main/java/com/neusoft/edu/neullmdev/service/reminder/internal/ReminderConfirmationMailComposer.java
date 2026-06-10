package com.neusoft.edu.neullmdev.service.reminder.internal;

import org.springframework.stereotype.Component;

/** 提醒确认邮件正文（用户点卡片确认后立即发送）。 */
@Component
public class ReminderConfirmationMailComposer {

    public String compose(String eventName, String eventDate, String eventTime, String description, int advanceMinutes) {
        StringBuilder content = new StringBuilder();
        content.append("你好呀～\n\n");
        content.append("智学伴已经帮你记下这条提醒啦。\n\n");
        content.append("【提醒事项】").append(eventName).append("\n");
        content.append("【计划时间】").append(eventDate).append(" ").append(eventTime).append("\n");
        content.append("【提前提醒】").append(advanceMinutes).append(" 分钟（约在 ")
                .append(eventDate).append(" ").append(eventTime).append(" 之前提醒你）\n");
        if (description != null && !description.trim().isEmpty()) {
            content.append("【备注】").append(description).append("\n");
        }
        content.append("\n到点前咱们一起加油～\n\n");
        content.append("智学伴");
        return content.toString();
    }

    public String confirmationSubject(String eventName) {
        return "【智学伴·提醒】" + eventName;
    }
}
