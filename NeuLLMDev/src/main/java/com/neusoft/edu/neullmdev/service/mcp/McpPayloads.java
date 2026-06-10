package com.neusoft.edu.neullmdev.service.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;

import java.util.Map;

/** 将 {@link ToolResult} 转为 Agent/前端沿用的 JSON 字符串。 */
public final class McpPayloads {

    private McpPayloads() {
    }

    public static String toAgentJson(ToolResult result, ObjectMapper objectMapper) {
        if (result == null) {
            return "{\"error\":\"empty tool result\"}";
        }
        Object data = result.getData();
        if (data instanceof String s && !s.isBlank()) {
            return s;
        }
        try {
            if (data != null) {
                return objectMapper.writeValueAsString(data);
            }
            return objectMapper.writeValueAsString(Map.of(
                    "functionName", result.getToolName(),
                    "message", result.getSummary() != null ? result.getSummary() : ""
            ));
        } catch (Exception e) {
            return "{\"error\":\"JSON序列化失败\"}";
        }
    }
}
