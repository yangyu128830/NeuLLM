package com.neusoft.edu.neullmdev.tool.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;

import java.util.Map;

public final class McpToolSupport {

    private McpToolSupport() {
    }

    public static ToolResult fromJsonString(String toolName, String json) {
        String summary = json != null && json.length() > 500 ? json.substring(0, 500) + "…" : json;
        return new ToolResult(toolName, summary != null ? summary : "", json);
    }

    public static ToolResult error(String toolName, String message) {
        return new ToolResult(toolName, message, Map.of("error", true, "message", message));
    }

    public static ToolResult validationError(String toolName, String message) {
        return new ToolResult(toolName, message, null);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String stringParam(Map<String, Object> params, String... keys) {
        if (params == null) {
            return "";
        }
        for (String key : keys) {
            Object v = params.get(key);
            if (v != null) {
                String s = v.toString().trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        }
        return "";
    }

    public static String writeJson(ObjectMapper objectMapper, Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"error\":true,\"message\":\"JSON序列化失败\"}";
        }
    }

    public static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
