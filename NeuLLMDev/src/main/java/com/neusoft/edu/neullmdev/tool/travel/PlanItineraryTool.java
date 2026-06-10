package com.neusoft.edu.neullmdev.tool.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.travel.PlanItineraryParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.travel.PlanItineraryService;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：行程规划委托 {@link PlanItineraryService}。 */
@Component
public class PlanItineraryTool implements McpToolHandler {

    private final PlanItineraryService planItineraryService;
    private final ObjectMapper objectMapper;

    public PlanItineraryTool(PlanItineraryService planItineraryService, ObjectMapper objectMapper) {
        this.planItineraryService = planItineraryService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "plan_itinerary";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        PlanItineraryParams params = objectMapper.convertValue(arguments, PlanItineraryParams.class);
        return planItineraryService.plan(params);
    }
}
