package com.neusoft.edu.neullmdev.tool.food;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.food.FoodSearchParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.food.FoodSearchService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：美食搜索委托 {@link FoodSearchService}。 */
@Component
public class FoodSearchTool implements McpToolHandler {

    private final FoodSearchService foodSearchService;
    private final ObjectMapper objectMapper;

    public FoodSearchTool(FoodSearchService foodSearchService, ObjectMapper objectMapper) {
        this.foodSearchService = foodSearchService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "food_search";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        FoodSearchParams params = objectMapper.convertValue(arguments, FoodSearchParams.class);
        if (McpToolSupport.isBlank(params.getKeyword())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：keyword 为必填项");
        }
        return foodSearchService.searchAsToolResult(params);
    }
}
