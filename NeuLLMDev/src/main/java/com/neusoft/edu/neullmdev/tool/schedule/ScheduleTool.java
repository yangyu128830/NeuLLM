package com.neusoft.edu.neullmdev.tool.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.schedule.ScheduleParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.schedule.ScheduleService;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：日程（演示）委托 {@link ScheduleService}。 */
@Component
public class ScheduleTool implements McpToolHandler {

    private final ScheduleService scheduleService;
    private final ObjectMapper objectMapper;

    public ScheduleTool(ScheduleService scheduleService, ObjectMapper objectMapper) {
        this.scheduleService = scheduleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "add_schedule";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        ScheduleParams params = objectMapper.convertValue(arguments, ScheduleParams.class);
        if (McpToolSupport.isBlank(params.getTitle())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：title 为必填项");
        }
        return scheduleService.addSchedule(params);
    }
}
