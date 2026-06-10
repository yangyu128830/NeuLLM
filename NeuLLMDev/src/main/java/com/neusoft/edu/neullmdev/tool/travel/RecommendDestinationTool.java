package com.neusoft.edu.neullmdev.tool.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.travel.RecommendDestinationParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.travel.RecommendDestinationService;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：目的地推荐委托 {@link RecommendDestinationService}。 */
@Component
public class RecommendDestinationTool implements McpToolHandler {

    private final RecommendDestinationService recommendDestinationService;
    private final ObjectMapper objectMapper;

    public RecommendDestinationTool(RecommendDestinationService recommendDestinationService, ObjectMapper objectMapper) {
        this.recommendDestinationService = recommendDestinationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "recommend_destination";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        RecommendDestinationParams params = objectMapper.convertValue(arguments, RecommendDestinationParams.class);
        return recommendDestinationService.recommend(params);
    }
}
