package com.neusoft.edu.neullmdev.tool.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.communication.SmsParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.communication.SmsService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：发短信委托 {@link SmsService}。 */
@Component
public class SendSmsTool implements McpToolHandler {

    private final SmsService smsService;
    private final ObjectMapper objectMapper;

    public SendSmsTool(SmsService smsService, ObjectMapper objectMapper) {
        this.smsService = smsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "send_sms";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        SmsParams params = objectMapper.convertValue(arguments, SmsParams.class);
        if (McpToolSupport.isBlank(params.getPhoneNumber())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：phoneNumber 为必填项");
        }
        if (McpToolSupport.isBlank(params.getMessage())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：message 为必填项");
        }
        return smsService.send(params);
    }
}
