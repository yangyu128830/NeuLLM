package com.neusoft.edu.neullmdev.controller.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.neusoft.edu.neullmdev.dto.communication.EmailParams;
import com.neusoft.edu.neullmdev.dto.hotel.HotelBookingParams;
import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import com.neusoft.edu.neullmdev.service.mcp.McpCommitService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户在前端卡片确认后，以 committed 上下文提交 MCP 副作用工具。
 */
@RestController
@RequestMapping("/api")
public class McpConfirmController {

    private final McpCommitService mcpCommitService;

    public McpConfirmController(McpCommitService mcpCommitService) {
        this.mcpCommitService = mcpCommitService;
    }

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailParams params) {
        return mcpCommitService.commitToolAsJson("send_email", params);
    }

    @PostMapping(value = "/hotelBook/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode confirmHotelBook(@RequestBody HotelBookingParams params) throws Exception {
        return mcpCommitService.commitTool("hotel_book", params);
    }

    @PostMapping(value = "/travelReminder/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode confirmTravelReminder(@RequestBody TravelReminderParams params) throws Exception {
        return mcpCommitService.commitTool("create_travel_reminder", params);
    }
}
