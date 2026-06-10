package com.neusoft.edu.neullmdev.service.reminder.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 合并模型返回的 remindTime、events 等非标准结构与草稿字段。
 */
@Component
public class ReminderStructuredPayloadMerger {

    private final ObjectMapper objectMapper;
    private final ReminderDatetimeParsing datetimeParsing;

    public ReminderStructuredPayloadMerger(ObjectMapper objectMapper, ReminderDatetimeParsing datetimeParsing) {
        this.objectMapper = objectMapper;
        this.datetimeParsing = datetimeParsing;
    }

    public void mergeRemindTime(TravelReminderParams params, ReminderDraft draft) {
        Object remindTimeObj = params.getRemindTime();
        if (remindTimeObj == null) {
            return;
        }
        String remindTime = remindTimeObj.toString();
        if (remindTime.trim().isEmpty() || "0".equals(remindTime)) {
            return;
        }
        if (remindTime.contains(" ")) {
            String[] parts = remindTime.split(" ");
            if (parts.length == 2) {
                draft.setEventDate(datetimeParsing.fixYear(parts[0]));
                draft.setEventTime(datetimeParsing.cleanTime(parts[1]));
            }
        } else if (remindTime.contains("T")) {
            String[] parts = remindTime.split("T");
            if (parts.length == 2) {
                draft.setEventDate(datetimeParsing.fixYear(parts[0]));
                draft.setEventTime(datetimeParsing.cleanTime(parts[1]));
            }
        }
    }

    public void mergeEventsObject(TravelReminderParams params, ReminderDraft draft) {
        Object events = params.getEvents();
        if (events == null) {
            return;
        }
        try {
            String eventsJson = objectMapper.writeValueAsString(events);
            if (eventsJson.startsWith("[")) {
                List<Map<String, Object>> eventsList = objectMapper.readValue(eventsJson,
                        new TypeReference<>() {
                        });
                if (!eventsList.isEmpty()) {
                    Map<String, Object> first = eventsList.get(0);
                    String event = (String) first.get("event");
                    String time = (String) first.get("time");
                    if (event != null && !event.trim().isEmpty()
                            && (draft.getEventName() == null || draft.getEventName().trim().isEmpty())) {
                        draft.setEventName(event);
                    }
                    if (time != null && !time.trim().isEmpty()
                            && (draft.getEventTime() == null || draft.getEventTime().trim().isEmpty())) {
                        draft.setEventTime(time);
                    }
                }
            } else {
                Map<String, Object> eventsMap = objectMapper.readValue(eventsJson, new TypeReference<>() {
                });
                Object from = eventsMap.get("$from");
                if (from != null) {
                    String fromJson = objectMapper.writeValueAsString(from);
                    Map<String, Object> fromMap = objectMapper.readValue(fromJson, new TypeReference<>() {
                    });
                    String time = (String) fromMap.get("time");
                    String content = (String) fromMap.get("content");
                    String type = (String) fromMap.get("type");
                    if (time != null && time.contains("T")) {
                        String[] parts = time.split("T");
                        if (parts.length == 2) {
                            if (draft.getEventDate() == null || draft.getEventDate().trim().isEmpty()) {
                                draft.setEventDate(parts[0]);
                            }
                            if (draft.getEventTime() == null || draft.getEventTime().trim().isEmpty()) {
                                draft.setEventTime(parts[1]);
                            }
                        }
                    }
                    if (content != null && !content.trim().isEmpty()
                            && (draft.getDescription() == null || draft.getDescription().trim().isEmpty())) {
                        draft.setDescription(content);
                    }
                    if (type != null && !type.trim().isEmpty()
                            && (draft.getEventName() == null || draft.getEventName().trim().isEmpty())) {
                        draft.setEventName(type);
                    }
                }
            }
        } catch (Exception e) {
            String eventsStr = events.toString();
            if (draft.getEventName() == null || draft.getEventName().trim().isEmpty()) {
                draft.setEventName(eventsStr.length() > 20 ? eventsStr.substring(0, 20) + "..." : eventsStr);
            }
            if (draft.getDescription() == null || draft.getDescription().trim().isEmpty()) {
                draft.setDescription(eventsStr);
            }
        }
    }

    public String buildCombinedTextForFallback(ReminderDraft draft, Object events) {
        StringBuilder combined = new StringBuilder();
        if (draft.getDescription() != null) {
            combined.append(draft.getDescription());
        }
        if (events != null) {
            try {
                String eventsJson = objectMapper.writeValueAsString(events);
                if (eventsJson.startsWith("[")) {
                    List<String> eventsList = objectMapper.readValue(eventsJson, new TypeReference<>() {
                    });
                    for (String item : eventsList) {
                        combined.append(" ").append(item);
                    }
                } else {
                    combined.append(" ").append(events);
                }
            } catch (Exception e) {
                combined.append(" ").append(events);
            }
        }
        return combined.toString();
    }
}
