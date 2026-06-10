package com.neusoft.edu.neullmdev.service.reminder.internal;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import org.springframework.stereotype.Component;

/**
 * 将用户自然语言 + LLM 工具参数解析为 {@link ReminderResolvedFields}（提醒智能体 · 字段解析子模块）。
 */
@Component
public class ReminderFieldResolver {

    private final ReminderEmailExtractor emailExtractor;
    private final ReminderDatetimeParsing datetimeParsing;
    private final ReminderStructuredPayloadMerger structuredPayloadMerger;
    private final ReminderToolResponseFactory responseFactory;

    public ReminderFieldResolver(
            ReminderEmailExtractor emailExtractor,
            ReminderDatetimeParsing datetimeParsing,
            ReminderStructuredPayloadMerger structuredPayloadMerger,
            ReminderToolResponseFactory responseFactory) {
        this.emailExtractor = emailExtractor;
        this.datetimeParsing = datetimeParsing;
        this.structuredPayloadMerger = structuredPayloadMerger;
        this.responseFactory = responseFactory;
    }

    public ReminderResolveOutcome resolve(TravelReminderParams params, String userInput) {
        if (params == null) {
            params = new TravelReminderParams();
        }

        datetimeParsing.applyExplicitDatetime(params);

        ReminderDraft draft = ReminderDraft.fromParams(
                params.getEventName(),
                params.getEventDate(),
                params.getEventTime(),
                params.getPhoneNumber(),
                params.getEmail(),
                params.getDescription(),
                params.getNotifyMethod());

        if (userInput != null && !userInput.trim().isEmpty()) {
            String extractedEmail = emailExtractor.extractFrom(userInput);
            if (extractedEmail != null && !extractedEmail.trim().isEmpty()) {
                draft.setEmail(extractedEmail);
            }
            if (draft.getEventDate() == null || draft.getEventDate().trim().isEmpty()) {
                String d = datetimeParsing.extractDateFromNaturalLanguage(userInput);
                if (d != null) {
                    draft.setEventDate(d);
                }
            }
            if (draft.getEventTime() == null || draft.getEventTime().trim().isEmpty()) {
                String t = datetimeParsing.extractTimeFromNaturalLanguage(userInput);
                if (t != null) {
                    draft.setEventTime(t);
                }
            }
            if (draft.getDescription() == null || draft.getDescription().trim().isEmpty()) {
                draft.setDescription(userInput);
            }
        }

        structuredPayloadMerger.mergeRemindTime(params, draft);
        structuredPayloadMerger.mergeEventsObject(params, draft);

        Object events = params.getEvents();
        if (draft.getEventName() == null || draft.getEventName().trim().isEmpty()) {
            if (events != null && !events.toString().trim().isEmpty()) {
                String eventsStr = events.toString();
                draft.setEventName(eventsStr.length() > 20 ? eventsStr.substring(0, 20) + "..." : eventsStr);
            } else if (draft.getDescription() != null && !draft.getDescription().trim().isEmpty()) {
                String desc = draft.getDescription();
                draft.setEventName(desc.length() > 20 ? desc.substring(0, 20) + "..." : desc);
            } else {
                draft.setEventName("学习提醒");
            }
        }

        String combinedText = structuredPayloadMerger.buildCombinedTextForFallback(draft, events);

        if (draft.getEventDate() == null || draft.getEventDate().trim().isEmpty()) {
            String extractedDate = datetimeParsing.extractDateFromNaturalLanguage(combinedText);
            if (extractedDate == null && draft.getDescription() != null) {
                extractedDate = datetimeParsing.extractDateFromNaturalLanguage(draft.getDescription());
            }
            if (extractedDate != null) {
                draft.setEventDate(extractedDate);
            } else {
                return new ReminderResolveOutcome.Err(responseFactory.missingDateErrorJson());
            }
        }

        if (draft.getEventTime() == null || draft.getEventTime().trim().isEmpty()) {
            String extractedTime = datetimeParsing.extractTimeFromNaturalLanguage(combinedText);
            if (extractedTime != null) {
                draft.setEventTime(extractedTime);
            }
        }

        draft.setEventDate(datetimeParsing.normalizeChineseOrLooseDate(draft.getEventDate()));

        if (draft.getEventTime() == null || draft.getEventTime().trim().isEmpty()) {
            draft.setEventTime("09:00");
        }

        ReminderResolvedFields resolved = draft.toResolved();
        return new ReminderResolveOutcome.Ok(resolved);
    }
}
