package com.neusoft.edu.neullmdev.dto.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 对话流请求：兼容纯文本 body 与 JSON {@code {"message":"...","teacherMode":true}}。
 */
public record ChatStreamRequest(String message, boolean teacherMode) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ChatStreamRequest parse(String body) {
        if (body == null) {
            return new ChatStreamRequest("", false);
        }
        String trimmed = body.trim();
        if (trimmed.startsWith("{")) {
            try {
                JsonNode node = MAPPER.readTree(trimmed);
                String msg = textField(node, "message");
                if (msg.isBlank()) {
                    msg = textField(node, "text");
                }
                if (msg.isBlank()) {
                    msg = textField(node, "prompt");
                }
                boolean teacher = node.path("teacherMode").asBoolean(false);
                return new ChatStreamRequest(msg, teacher);
            } catch (Exception ignored) {
                return new ChatStreamRequest(trimmed, false);
            }
        }
        return new ChatStreamRequest(trimmed, false);
    }

    private static String textField(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n != null && !n.isNull() ? n.asText("").trim() : "";
    }
}
