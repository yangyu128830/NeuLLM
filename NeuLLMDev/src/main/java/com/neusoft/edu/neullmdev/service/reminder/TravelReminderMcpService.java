package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpFunctionCalls;
import com.neusoft.edu.neullmdev.service.mcp.McpService;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderConfirmationMailComposer;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderToolResponseFactory;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderAdvanceMinutesPolicy;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderDatetimeParsing;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderFieldResolver;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderResolveOutcome;
import com.neusoft.edu.neullmdev.service.reminder.internal.ReminderResolvedFields;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * create_travel_reminder 工具的业务编排（preview / committed），供 {@link com.neusoft.edu.neullmdev.tool.reminder.TravelReminderTool} 委托。
 */
@Slf4j
@Service
public class TravelReminderMcpService {

    private final TravelReminderService travelReminderService;
    private final ReminderFieldResolver fieldResolver;
    private final ReminderDatetimeParsing datetimeParsing;
    private final ReminderAdvanceMinutesPolicy advanceMinutesPolicy;
    private final ReminderToolResponseFactory responseFactory;
    private final ReminderConfirmationMailComposer mailComposer;
    private final McpService mcpService;

    public TravelReminderMcpService(TravelReminderService travelReminderService,
                                    ReminderFieldResolver fieldResolver,
                                    ReminderDatetimeParsing datetimeParsing,
                                    ReminderAdvanceMinutesPolicy advanceMinutesPolicy,
                                    ReminderToolResponseFactory responseFactory,
                                    ReminderConfirmationMailComposer mailComposer,
                                    @Lazy McpService mcpService) {
        this.travelReminderService = travelReminderService;
        this.fieldResolver = fieldResolver;
        this.datetimeParsing = datetimeParsing;
        this.advanceMinutesPolicy = advanceMinutesPolicy;
        this.responseFactory = responseFactory;
        this.mailComposer = mailComposer;
        this.mcpService = mcpService;
    }

    public ToolResult execute(TravelReminderParams params, McpCallContext ctx) {
        McpCallContext safeCtx = ctx != null ? ctx : McpCallContext.http();
        String userInput = safeCtx.userInput();

        ReminderResolveOutcome outcome = fieldResolver.resolve(params, userInput);
        if (outcome instanceof ReminderResolveOutcome.Err err) {
            return McpToolSupport.fromJsonString("create_travel_reminder", err.jsonPayload());
        }
        ReminderResolvedFields fields = ((ReminderResolveOutcome.Ok) outcome).fields();
        final int advanceMin = advanceMinutesPolicy.resolve(params != null ? params : new TravelReminderParams());

        if (!safeCtx.commitSideEffects()) {
            String json = responseFactory.previewFormJson(fields, advanceMin);
            return new ToolResult("create_travel_reminder", "学习提醒预填卡片", json);
        }

        try {
            LocalDateTime eventDateTime = datetimeParsing.parseEventDateTime(fields.eventDate(), fields.eventTime());
            LocalDateTime reminderDateTime = eventDateTime.minusMinutes(advanceMin);

            TravelReminderEntity reminder = new TravelReminderEntity();
            reminder.setId(UUID.randomUUID().toString());
            reminder.setEventName(fields.eventName());
            reminder.setEventDate(fields.eventDate());
            reminder.setEventTime(fields.eventTime());
            reminder.setEmail(fields.email());
            reminder.setPhoneNumber(fields.phoneNumber());
            reminder.setDescription(fields.description());
            reminder.setReminderMinutes(advanceMin);
            reminder.setRepeatDaily(resolveRepeatDaily(params, userInput));
            reminder.setCreatedAt(LocalDateTime.now());
            reminder.setUpdatedAt(LocalDateTime.now());

            boolean persistedOk = persistReminder(reminder);

            boolean emailSentNow = false;
            String emailAck = "";
            if (fields.email() != null && !fields.email().trim().isEmpty()) {
                String body = mailComposer.compose(
                        fields.eventName(), fields.eventDate(), fields.eventTime(),
                        fields.description(), advanceMin);
                ToolResult emailResult = mcpService.callTool(
                        "send_email",
                        McpFunctionCalls.sendEmailArgs(
                                fields.email(),
                                mailComposer.confirmationSubject(fields.eventName()),
                                body),
                        McpCallContext.committed(""));
                emailAck = emailResult.getData() instanceof String s ? s : emailResult.getSummary();
                emailSentNow = emailAck != null && emailAck.contains("成功");
            }

            String hint = buildUserHint(fields, advanceMin, persistedOk, emailSentNow,
                    Boolean.TRUE.equals(reminder.getRepeatDaily()));
            String json = responseFactory.successJson(
                    fields, eventDateTime, reminderDateTime, advanceMin,
                    persistedOk, emailSentNow, emailAck, hint);
            return McpToolSupport.fromJsonString("create_travel_reminder", json);
        } catch (DateTimeParseException e) {
            return McpToolSupport.fromJsonString("create_travel_reminder", responseFactory.parseErrorJson());
        }
    }

    private boolean persistReminder(TravelReminderEntity reminder) {
        try {
            travelReminderService.save(reminder);
            return true;
        } catch (Exception e) {
            log.warn("数据库保存失败（可能未配置）: {}", e.getMessage());
            return false;
        }
    }

    private static boolean resolveRepeatDaily(TravelReminderParams params, String userInput) {
        if (params != null && params.getRepeatDaily() != null) {
            return Boolean.TRUE.equals(params.getRepeatDaily());
        }
        if (userInput != null && userInput.matches(".*(每天|每晚|每天晚上|每个晚上).*")) {
            return true;
        }
        return false;
    }

    private static String buildUserHint(
            ReminderResolvedFields fields,
            int advanceMinutes,
            boolean persistedOk,
            boolean emailSentNow,
            boolean repeatDaily) {
        if (repeatDaily && persistedOk) {
            return "已开启每天重复；到点前 "
                    + advanceMinutes + " 分钟会发邮件提醒你（需填写有效邮箱）。"
                    + (emailSentNow ? " 确认邮件已发送。" : "");
        }
        if (emailSentNow) {
            return "已按你的邮箱发送一封确认邮件；到点前 "
                    + advanceMinutes + " 分钟还会由定时任务再提醒你一次（若服务已配置邮箱）。";
        }
        String phoneNumber = fields.phoneNumber();
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return "已记录手机号；邮件通道未使用时将以日志形式输出提醒内容。";
        }
        if (persistedOk) {
            return "还没有邮箱也没关系～提醒已经记在系统里了，到时间会由后台定时任务尝试通知（配置邮箱后可收邮件）。";
        }
        return "本次未能写入数据库时，仍以对话结果为准；建议你补充邮箱以便邮件提醒。";
    }
}
