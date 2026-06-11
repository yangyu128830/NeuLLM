package com.neusoft.edu.neullmdev.service.notification;

import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationSummaryService {

    private static final String SYSTEM_PROMPT = """
            你是「智学伴」，帮学生用温暖、口语的方式总结站内消息概况。
            根据用户消息里提供的 JSON 数据作答，要求：
            ● 2～4 句话，像朋友发微信，不要 markdown、不要 1.2.3. 编号。
            ● 先说有没有未读、未读大概是什么（作业催交/新作业/批改等），要不要马上看。
            ● 若 unreadCount 为 0 且 totalCount 大于 0：轻松告诉用户「暂时没有新消息，之前的都看过了」。
            ● 若 totalCount 为 0：友好说明还没有任何通知，教师发布作业或批改后会出现在消息中心。
            ● 严禁编造 JSON 里没有的通知标题或数量。
            """;

    private final NotificationService notificationService;
    private final KimiChatService kimiChatService;

    public NotificationSummaryService(NotificationService notificationService,
                                      KimiChatService kimiChatService) {
        this.notificationService = notificationService;
        this.kimiChatService = kimiChatService;
    }

    public Map<String, Object> summarizeForCurrentUser(int limit) {
        int cap = Math.min(Math.max(limit, 1), 50);
        List<Map<String, Object>> all = notificationService.listForCurrentUser(cap);
        int unread = notificationService.unreadCountForCurrentUser();

        List<Map<String, Object>> unreadItems = new ArrayList<>();
        for (Map<String, Object> row : all) {
            if (Boolean.FALSE.equals(row.get("read"))) {
                unreadItems.add(row);
            }
        }

        Map<String, Integer> typeCounts = countByTypeLabel(all);
        String summary = composeSummary(all.size(), unread, unreadItems, typeCounts);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("unread", unread);
        out.put("total", all.size());
        out.put("unreadItems", unreadItems);
        out.put("typeCounts", typeCounts);
        out.put("empty", all.isEmpty());
        out.put("allCaughtUp", !all.isEmpty() && unread == 0);
        return out;
    }

    private String composeSummary(int total, int unread, List<Map<String, Object>> unreadItems,
                                    Map<String, Integer> typeCounts) {
        if (total == 0) {
            return fallbackEmpty();
        }
        try {
            JSONObject payload = new JSONObject()
                    .put("totalCount", total)
                    .put("unreadCount", unread)
                    .put("typeCounts", new JSONObject(typeCounts));
            JSONArray unreadArr = new JSONArray();
            for (Map<String, Object> item : unreadItems.stream().limit(8).toList()) {
                unreadArr.put(new JSONObject()
                        .put("typeLabel", String.valueOf(item.getOrDefault("typeLabel", "通知")))
                        .put("title", String.valueOf(item.getOrDefault("title", "")))
                        .put("createdAt", String.valueOf(item.getOrDefault("createdAt", ""))));
            }
            payload.put("unreadItems", unreadArr);

            String raw = kimiChatService.chatWithSystem(SYSTEM_PROMPT, payload.toString(), 0.45)
                    .block(Duration.ofSeconds(45));
            if (raw != null && !raw.isBlank() && !raw.startsWith("解析响应出错")) {
                return raw.trim();
            }
        } catch (Exception e) {
            log.warn("消息 AI 总结失败，使用规则兜底: {}", e.getMessage());
        }
        return fallbackSummary(total, unread, unreadItems, typeCounts);
    }

    private static Map<String, Integer> countByTypeLabel(List<Map<String, Object>> all) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : all) {
            String label = String.valueOf(row.getOrDefault("typeLabel", "通知"));
            counts.merge(label, 1, Integer::sum);
        }
        return counts;
    }

    private static String fallbackEmpty() {
        return "你这边还没有收到任何消息～等老师发布作业、催交或批改完成后，通知会自动出现在消息中心，到时候我再来帮你汇总。";
    }

    private static String fallbackSummary(int total, int unread, List<Map<String, Object>> unreadItems,
                                          Map<String, Integer> typeCounts) {
        if (unread == 0) {
            return "暂时没有新的未读消息，之前的通知你都看过了～要是想翻历史记录，可以点下面进消息中心。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("你有 ").append(unread).append(" 条还没读");
        if (!typeCounts.isEmpty()) {
            sb.append("；最近 ").append(total).append(" 条里包括 ");
            sb.append(String.join("、", typeCounts.entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue() + " 条")
                    .toList()));
        }
        sb.append("。");
        if (!unreadItems.isEmpty()) {
            sb.append("最需要先看的是「").append(unreadItems.get(0).getOrDefault("title", "最新通知")).append("」。");
        }
        return sb.toString();
    }
}
