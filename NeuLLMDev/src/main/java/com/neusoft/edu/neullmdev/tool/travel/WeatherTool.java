package com.neusoft.edu.neullmdev.tool.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.travel.WeatherParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.travel.WeatherService;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：天气查询委托 {@link WeatherService}。 */
@Component
public class WeatherTool implements McpToolHandler {

    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;

    public WeatherTool(WeatherService weatherService, ObjectMapper objectMapper) {
        this.weatherService = weatherService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "get_current_weather";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        WeatherParams params = objectMapper.convertValue(arguments, WeatherParams.class);
        if (McpToolSupport.isBlank(params.getLocation()) && McpToolSupport.isBlank(params.getAdcode())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：location 或 adcode 至少需要提供一个");
        }
        return weatherService.query(params);
    }
}
