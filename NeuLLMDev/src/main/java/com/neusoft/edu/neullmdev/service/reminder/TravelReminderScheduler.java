package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import com.neusoft.edu.neullmdev.mapper.reminder.TravelReminderMapper;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.service.mcp.McpFunctionCalls;
import com.neusoft.edu.neullmdev.service.mcp.McpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
public class TravelReminderScheduler {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TravelReminderMapper travelReminderMapper;
    private final McpService mcpService;

    public TravelReminderScheduler(TravelReminderMapper travelReminderMapper, McpService mcpService) {
        this.travelReminderMapper = travelReminderMapper;
        this.mcpService = mcpService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        try {
            List<TravelReminderEntity> pending = travelReminderMapper.selectPending();
            for (TravelReminderEntity reminder : pending) {
                LocalDateTime reminderTime = resolveReminderTime(reminder);
                if (reminderTime == null) {
                    continue;
                }
                if (isStale(now, reminderTime)) {
                    expireReminder(reminder, now);
                    continue;
                }
                if (shouldSendNow(now, reminderTime)) {
                    sendReminderNotification(reminder, now);
                }
            }
        } catch (Exception e) {
            log.warn("定时任务执行异常（数据库可能未配置）: {}", e.getMessage());
        }
    }

    private LocalDateTime resolveReminderTime(TravelReminderEntity reminder) {
        if (reminder.getEventDate() == null || reminder.getEventTime() == null) {
            return null;
        }
        try {
            String dateTimeStr = reminder.getEventDate().trim() + " " + normalizeTime(reminder.getEventTime());
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析提醒时间失败 id={}: {}", reminder.getId(), e.getMessage());
            return null;
        }
    }

    private static String normalizeTime(String eventTime) {
        String t = eventTime.trim();
        if (t.length() >= 5) {
            return t.substring(0, 5);
        }
        return t;
    }

    private static boolean shouldSendNow(LocalDateTime now, LocalDateTime reminderTime) {
        return !now.isBefore(reminderTime.minusMinutes(1)) && now.isBefore(reminderTime.plusMinutes(1));
    }

    /** 错过发送窗口后标记完成，避免无限扫描；每日重复的不在此过期（会滚动到下一天）。 */
    private static boolean isStale(LocalDateTime now, LocalDateTime reminderTime) {
        return now.isAfter(reminderTime.plusMinutes(5));
    }

    private void expireReminder(TravelReminderEntity reminder, LocalDateTime now) {
        if (Boolean.TRUE.equals(reminder.getRepeatDaily())) {
            rollToNextDay(reminder, now);
            return;
        }
        travelReminderMapper.markNotified(reminder.getId(), now);
        log.info("提醒已过期未发送，标记完成：{} {}", reminder.getEventName(), reminder.getId());
    }

    private void sendReminderNotification(TravelReminderEntity reminder, LocalDateTime now) {
        String emailSubject = "【智学伴·学习提醒】" + reminder.getEventName();
        String emailContent = buildEmailContent(reminder);
        log.info("触发定时邮件提醒：{} - {}", reminder.getEventName(), now);

        boolean sent = false;
        if (reminder.getEmail() != null && !reminder.getEmail().trim().isEmpty()) {
            String result = mcpService.callToolAsJson(
                    "send_email",
                    McpFunctionCalls.sendEmailArgs(reminder.getEmail(), emailSubject, emailContent),
                    McpCallContext.committed(""));
            sent = result != null && result.contains("成功");
            log.info("邮件发送结果：{}", result);
        } else {
            log.info("提醒内容（无邮箱）：{}", emailContent);
            sent = true;
        }

        if (!sent) {
            return;
        }

        if (Boolean.TRUE.equals(reminder.getRepeatDaily())) {
            rollToNextDay(reminder, now);
        } else {
            travelReminderMapper.markNotified(reminder.getId(), now);
        }
    }

    private void rollToNextDay(TravelReminderEntity reminder, LocalDateTime now) {
        try {
            LocalDate nextDate = LocalDate.parse(reminder.getEventDate().trim()).plusDays(1);
            reminder.setEventDate(nextDate.toString());
            reminder.setNotifiedAt(null);
            reminder.setUpdatedAt(now);
            travelReminderMapper.update(reminder);
            log.info("每日提醒已滚动至下一天：{} → {}", reminder.getEventName(), nextDate);
        } catch (DateTimeParseException e) {
            log.warn("每日提醒滚动失败 id={}: {}", reminder.getId(), e.getMessage());
            travelReminderMapper.markNotified(reminder.getId(), now);
        }
    }

    private String buildEmailContent(TravelReminderEntity reminder) {
        int advanceMin = reminder.getReminderMinutes() != null ? reminder.getReminderMinutes() : 15;
        String advanceLabel = advanceMin == 0 ? "准时" : "提前 " + advanceMin + " 分钟";

        StringBuilder content = new StringBuilder();
        content.append("亲爱的用户，\n\n");
        content.append("到你设定的学习时间啦，来看看～\n\n");
        content.append("【事件名称】").append(reminder.getEventName()).append("\n");
        content.append("【事件时间】").append(reminder.getEventDate()).append(" ").append(reminder.getEventTime()).append("\n");
        content.append("【提醒设置】").append(advanceLabel).append("\n");
        if (Boolean.TRUE.equals(reminder.getRepeatDaily())) {
            content.append("【重复】每天同一时刻\n");
        }
        if (reminder.getDescription() != null && !reminder.getDescription().trim().isEmpty()) {
            content.append("【备注信息】").append(reminder.getDescription()).append("\n");
        }
        content.append("\n加油，咱们继续一起学～\n\n");
        content.append("智学伴");
        return content.toString();
    }
}
