package com.neusoft.edu.neullmdev.controller.mcp;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP JSON-RPC 端点（与教学模板 mcp-server-starter 一致）。
 * POST /mcp/jsonrpc
 */
@RestController
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @PostMapping(value = "/mcp/jsonrpc", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handle(@RequestBody Map<String, Object> request) {
        Object id = request.get("id");
        String jsonrpc = String.valueOf(request.getOrDefault("jsonrpc", ""));
        String method = String.valueOf(request.getOrDefault("method", ""));

        if (!"2.0".equals(jsonrpc)) {
            return error(id, -32600, "jsonrpc must be 2.0");
        }

        try {
            return switch (method) {
                case "initialize" -> success(id, Map.of(
                        "protocolVersion", "2025-11-25",
                        "serverInfo", Map.of(
                                "name", "neullm-travel-assistant-mcp",
                                "version", "1.0.0"
                        ),
                        "capabilities", Map.of(
                                "tools", Map.of()
                        )
                ));
                case "tools/list" -> success(id, Map.of("tools", mcpService.getTools()));
                case "tools/call" -> handleToolCall(id, request);
                default -> error(id, -32601, "method not found: " + method);
            };
        } catch (Exception e) {
            return error(id, -32603, "internal error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolCall(Object id, Map<String, Object> request) {
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());
        String name = String.valueOf(params.getOrDefault("name", ""));
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        if (name.isBlank()) {
            return error(id, -32602, "params.name is required");
        }

        ToolResult toolResult = mcpService.callTool(name, arguments, McpCallContext.http());
        return success(id, Map.of(
                "content", java.util.List.of(Map.of(
                        "type", "text",
                        "text", toolResult.getSummary()
                )),
                "structuredContent", toolResult
        ));
    }

    private Map<String, Object> success(Object id, Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    private Map<String, Object> error(Object id, int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }
}
