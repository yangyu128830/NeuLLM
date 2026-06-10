package com.neusoft.edu.neullmdev.tool.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.communication.EmailParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.communication.SendEmailService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：发邮件委托 {@link SendEmailService}。 */
@Component
public class SendEmailTool implements McpToolHandler {

    private final SendEmailService sendEmailService;
    private final ObjectMapper objectMapper;

    public SendEmailTool(SendEmailService sendEmailService, ObjectMapper objectMapper) {
        this.sendEmailService = sendEmailService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "send_email";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        EmailParams params = objectMapper.convertValue(arguments, EmailParams.class);
        if (McpToolSupport.isBlank(params.getRecipient())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：recipient 为必填项");
        }
        McpCallContext ctx = context != null ? context : McpCallContext.http();
        return sendEmailService.send(params, ctx);
    }
}
