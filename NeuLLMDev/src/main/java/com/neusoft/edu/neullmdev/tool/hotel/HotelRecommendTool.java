package com.neusoft.edu.neullmdev.tool.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.hotel.HotelSearchParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.hotel.HotelRecommendService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：酒店推荐委托 {@link HotelRecommendService}。 */
@Component
public class HotelRecommendTool implements McpToolHandler {

    private final HotelRecommendService hotelRecommendService;
    private final ObjectMapper objectMapper;

    public HotelRecommendTool(HotelRecommendService hotelRecommendService, ObjectMapper objectMapper) {
        this.hotelRecommendService = hotelRecommendService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "hotel_recommend";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        HotelSearchParams params = objectMapper.convertValue(arguments, HotelSearchParams.class);
        return McpToolSupport.fromJsonString("hotel_recommend", hotelRecommendService.recommendHotels(params));
    }
}
