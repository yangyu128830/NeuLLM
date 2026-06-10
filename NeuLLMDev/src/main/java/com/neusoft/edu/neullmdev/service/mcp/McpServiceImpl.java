package com.neusoft.edu.neullmdev.service.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.McpTool;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class McpServiceImpl implements McpService {

    private final ObjectMapper objectMapper;
    private final McpToolCatalog toolCatalog;
    private final Map<String, McpToolHandler> handlersByName;

    public McpServiceImpl(ObjectMapper objectMapper,
                          McpToolCatalog toolCatalog,
                          List<McpToolHandler> handlers) {
        this.objectMapper = objectMapper;
        this.toolCatalog = toolCatalog;
        this.handlersByName = handlers.stream()
                .collect(Collectors.toMap(
                        h -> McpToolNames.normalize(h.toolName()),
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalStateException("重复的 MCP 工具 handler: " + a.toolName());
                        }));
    }

    @Override
    public List<McpTool> getTools() {
        return toolCatalog.listTools();
    }

    @Override
    public ToolResult callTool(String toolName, Map<String, Object> arguments, McpCallContext context) {
        Map<String, Object> safeArgs = arguments == null ? Map.of() : arguments;
        String name = McpToolNames.normalize(toolName);
        McpCallContext ctx = context != null ? context : McpCallContext.http();
        log.debug("[MCP] call tool: {}, arguments={}", name, safeArgs);

        McpToolHandler handler = handlersByName.get(name);
        if (handler == null) {
            return new ToolResult(name, "未知工具：" + toolName, Map.of("error", "未知函数: " + toolName));
        }
        return handler.handle(safeArgs, ctx);
    }

    @Override
    public String callToolAsJson(String toolName, Map<String, Object> arguments, McpCallContext context) {
        return McpPayloads.toAgentJson(callTool(toolName, arguments, context), objectMapper);
    }
}
