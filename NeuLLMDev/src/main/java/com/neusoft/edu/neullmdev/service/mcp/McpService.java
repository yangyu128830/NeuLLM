package com.neusoft.edu.neullmdev.service.mcp;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.McpTool;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * MCP 工具唯一入口（与教学模板 mcp-server-starter 一致）。
 * 业务代码、Agent、定时任务、HTTP 控制器均只通过本服务调用工具。
 */
public interface McpService {

    List<McpTool> getTools();

    default ToolResult callTool(String toolName, Map<String, Object> arguments) {
        return callTool(toolName, arguments, McpCallContext.http());
    }

    ToolResult callTool(String toolName, Map<String, Object> arguments, McpCallContext context);

    /** Agent SSE 等场景：经 {@link #callTool} 调用后转为 JSON 字符串。 */
    String callToolAsJson(String toolName, Map<String, Object> arguments, McpCallContext context);
}
