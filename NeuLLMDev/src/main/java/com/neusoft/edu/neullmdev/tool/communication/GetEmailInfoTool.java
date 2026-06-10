package com.neusoft.edu.neullmdev.tool.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.communication.EmailLookupParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.communication.EmailLookupService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：邮箱别名查询委托 {@link EmailLookupService}。 */
@Component
public class GetEmailInfoTool implements McpToolHandler {

    private final EmailLookupService emailLookupService;
    private final ObjectMapper objectMapper;

    public GetEmailInfoTool(EmailLookupService emailLookupService, ObjectMapper objectMapper) {
        this.emailLookupService = emailLookupService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "get_email_info";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        EmailLookupParams params = objectMapper.convertValue(arguments, EmailLookupParams.class);
        if (McpToolSupport.isBlank(params.getAccount())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：account 为必填项");
        }
        return emailLookupService.lookup(params);
    }
}
