package com.neusoft.edu.neullmdev.service.mcp;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;

import java.util.Map;

/** 单个 MCP 工具的分发器；由 {@link tool} 包中的类实现，Spring 自动注册到 {@link McpServiceImpl}。 */
public interface McpToolHandler {

    /** 标准工具名（与 {@link McpToolNames#normalize} 之后一致）。 */
    String toolName();

    ToolResult handle(Map<String, Object> arguments, McpCallContext context);
}
