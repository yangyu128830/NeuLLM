package com.neusoft.edu.neullmdev.tool.hotel;

import com.neusoft.edu.neullmdev.dto.hotel.HotelBookingParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.hotel.HotelBookService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：酒店预订委托 {@link HotelBookService}。 */
@Component
public class HotelBookTool implements McpToolHandler {

    private final HotelBookService hotelBookService;
    private final ObjectMapper objectMapper;

    public HotelBookTool(HotelBookService hotelBookService, ObjectMapper objectMapper) {
        this.hotelBookService = hotelBookService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "hotel_book";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        HotelBookingParams params = objectMapper.convertValue(arguments, HotelBookingParams.class);
        return McpToolSupport.fromJsonString("hotel_book",
                hotelBookService.executeBook(params, context.commitSideEffects()));
    }
}
