package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小模型常把「发邮件…主题是旅行提醒」误判为酒店/行程等。
 * 当用户原文已明确包含发邮件话术 + 邮箱时，用规则覆盖错误的 function 选择。
 */
@Component
public class SendEmailIntentOverride {

    private static final Pattern RECIPIENT = Pattern.compile(
            "(?:发邮件|发送邮件|写邮件|发一封邮件|发封邮件)\\s*(?:给|到|至)?\\s*([\\w.+-]+@[\\w.-]+\\.[a-zA-Z0-9]+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SUBJECT = Pattern.compile(
            "主题(?:是|为|[:：])?\\s*([^，,；;。\\n]+)");

    private static final Pattern CONTENT = Pattern.compile(
            "内容(?:是|为|[:：])?\\s*(.+)$", Pattern.DOTALL);

    private static final Set<String> OVERRIDE_WRONG_TOOLS = Set.of(
            "hotel_recommend",
            "hotel_book",
            "plan_itinerary",
            "recommend_destination",
            "get_current_weather",
            "getlocationinfo",
            "wordwrite",
            "create_travel_reminder",
            "settravelreminder"
    );

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        if (userInput == null || userInput.isBlank()) {
            return parsed;
        }
        if (!hasExplicitSendEmailWording(userInput)) {
            return parsed;
        }
        Optional<FunctionCall> forced = tryBuildFromUserText(userInput);
        if (forced.isEmpty()) {
            return parsed;
        }
        if (parsed == null || parsed.getName() == null || parsed.getName().isBlank()) {
            return forced.get();
        }
        String n = parsed.getName().toLowerCase().trim();
        if ("send_email".equals(n)) {
            return parsed;
        }
        if (OVERRIDE_WRONG_TOOLS.contains(n)) {
            return forced.get();
        }
        return parsed;
    }

    private boolean hasExplicitSendEmailWording(String s) {
        return s.contains("发邮件")
                || s.contains("发送邮件")
                || s.contains("写邮件")
                || s.contains("发一封邮件")
                || s.contains("发封邮件")
                || s.contains("邮件发给");
    }

    private Optional<FunctionCall> tryBuildFromUserText(String userInput) {
        Matcher rm = RECIPIENT.matcher(userInput);
        if (!rm.find()) {
            return Optional.empty();
        }
        String recipient = rm.group(1).trim();
        if (recipient.isEmpty()) {
            return Optional.empty();
        }
        String subject = "无主题";
        Matcher sm = SUBJECT.matcher(userInput);
        if (sm.find()) {
            String t = sm.group(1).trim();
            if (!t.isEmpty()) {
                subject = t;
            }
        }
        String content = "";
        Matcher cm = CONTENT.matcher(userInput);
        if (cm.find()) {
            content = cm.group(1).trim();
        }
        FunctionCall fc = new FunctionCall();
        fc.setName("send_email");
        Map<String, Object> params = new HashMap<>();
        params.put("recipient", recipient);
        params.put("subject", subject);
        params.put("content", content);
        fc.setParams(params);
        return Optional.of(fc);
    }
}
