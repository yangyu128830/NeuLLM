package com.neusoft.edu.neullmdev.tool.reminder;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.reminder.TravelReminderMcpService;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：委托 {@link TravelReminderMcpService} 执行业务。 */
@Component
public class TravelReminderTool implements McpToolHandler {

    private final TravelReminderMcpService travelReminderMcpService;
    private final ObjectMapper objectMapper;

    public TravelReminderTool(TravelReminderMcpService travelReminderMcpService, ObjectMapper objectMapper) {
        this.travelReminderMcpService = travelReminderMcpService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "create_travel_reminder";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        TravelReminderParams params = objectMapper.convertValue(arguments, TravelReminderParams.class);
        if (McpToolSupport.isBlank(params.getEventName()) && McpToolSupport.isBlank(params.getDatetime())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：eventName 或 datetime 至少需要提供一个");
        }
        return travelReminderMcpService.execute(params, context);
    }
}
