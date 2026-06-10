package com.neusoft.edu.neullmdev.tool.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.travel.LocationParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.travel.LocationService;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：地点坐标查询委托 {@link LocationService}。 */
@Component
public class LocationTool implements McpToolHandler {

    private final LocationService locationService;
    private final ObjectMapper objectMapper;

    public LocationTool(LocationService locationService, ObjectMapper objectMapper) {
        this.locationService = locationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "get_location_info";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        LocationParams params = objectMapper.convertValue(arguments, LocationParams.class);
        if (McpToolSupport.isBlank(params.getDestination()) && McpToolSupport.isBlank(params.getOrigin())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：destination 或 origin 至少需要提供一个");
        }
        return locationService.lookup(params);
    }
}
