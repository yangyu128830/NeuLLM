package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import com.neusoft.edu.neullmdev.mapper.reminder.TravelReminderMapper;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.service.mcp.McpFunctionCalls;
import com.neusoft.edu.neullmdev.service.mcp.McpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class TravelReminderScheduler {

    private final TravelReminderMapper travelReminderMapper;
    private final McpService mcpService;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TravelReminderScheduler(TravelReminderMapper travelReminderMapper, McpService mcpService) {
        this.travelReminderMapper = travelReminderMapper;
        this.mcpService = mcpService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        try {
            List<TravelReminderEntity> allReminders = travelReminderMapper.selectAll();

            for (TravelReminderEntity reminder : allReminders) {
                if (shouldSendReminder(reminder, now)) {
                    sendReminderNotification(reminder);
                }
            }
        } catch (Exception e) {
            log.warn("定时任务执行异常（数据库可能未配置）: {}", e.getMessage());
        }
    }

    private boolean shouldSendReminder(TravelReminderEntity reminder, LocalDateTime now) {
        if (reminder.getEventDate() == null || reminder.getEventTime() == null) {
            return false;
        }

        try {
            String dateTimeStr = reminder.getEventDate() + " " + reminder.getEventTime();
            LocalDateTime eventDateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            LocalDateTime reminderTime = eventDateTime.minusMinutes(reminder.getReminderMinutes() != null ? reminder.getReminderMinutes() : 15);

            return now.isAfter(reminderTime.minusMinutes(1)) && now.isBefore(reminderTime.plusMinutes(1));
        } catch (Exception e) {
            log.warn("解析提醒时间失败: {}", e.getMessage());
            return false;
        }
    }

    private void sendReminderNotification(TravelReminderEntity reminder) {
        String emailSubject = "【智学伴·学习提醒】" + reminder.getEventName();
        String emailContent = buildEmailContent(reminder);
        log.info("触发定时邮件提醒：{} - {}", reminder.getEventName(), LocalDateTime.now());

        if (reminder.getEmail() != null && !reminder.getEmail().trim().isEmpty()) {
            String result = mcpService.callToolAsJson(
                    "send_email",
                    McpFunctionCalls.sendEmailArgs(reminder.getEmail(), emailSubject, emailContent),
                    McpCallContext.committed(""));
            log.info("邮件发送结果：{}", result);
        } else {
            log.info("提醒内容（无邮箱）：{}", emailContent);
        }
    }

    private String buildEmailContent(TravelReminderEntity reminder) {
        StringBuilder content = new StringBuilder();
        content.append("亲爱的用户，\n\n");
        content.append("到你设定的学习时间啦，来看看～\n\n");
        content.append("【事件名称】").append(reminder.getEventName()).append("\n");
        content.append("【事件时间】").append(reminder.getEventDate()).append(" ").append(reminder.getEventTime()).append("\n");
        content.append("【提醒时间】提前15分钟\n");
        if (reminder.getDescription() != null && !reminder.getDescription().trim().isEmpty()) {
            content.append("【备注信息】").append(reminder.getDescription()).append("\n");
        }
        content.append("\n加油，咱们继续一起学～\n\n");
        content.append("智学伴");
        return content.toString();
    }
}
