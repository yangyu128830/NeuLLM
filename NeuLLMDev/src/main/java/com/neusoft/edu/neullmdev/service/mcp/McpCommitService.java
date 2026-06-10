package com.neusoft.edu.neullmdev.service.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户卡片确认后，以 committed 上下文调用 MCP 工具并解析为 JSON。
 */
@Service
public class McpCommitService {

    private final McpService mcpService;
    private final ObjectMapper objectMapper;

    public McpCommitService(McpService mcpService, ObjectMapper objectMapper) {
        this.mcpService = mcpService;
        this.objectMapper = objectMapper;
    }

    public JsonNode commitTool(String toolName, Object params) throws Exception {
        return objectMapper.readTree(commitToolAsJson(toolName, params));
    }

    public String commitToolAsJson(String toolName, Object params) {
        return mcpService.callToolAsJson(toolName, toArgumentMap(params), McpCallContext.committed(""));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toArgumentMap(Object params) {
        if (params instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return objectMapper.convertValue(params, Map.class);
    }
}
