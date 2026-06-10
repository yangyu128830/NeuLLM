package com.neusoft.edu.neullmdev.tool.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.profile.UserProfileSaveParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.service.profile.UserProfileSaveService;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：保存用户资料委托 {@link UserProfileSaveService}。 */
@Component
public class SaveUserProfileTool implements McpToolHandler {

    private final UserProfileSaveService userProfileSaveService;
    private final ObjectMapper objectMapper;

    public SaveUserProfileTool(UserProfileSaveService userProfileSaveService, ObjectMapper objectMapper) {
        this.userProfileSaveService = userProfileSaveService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "save_user_profile";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        UserProfileSaveParams params = objectMapper.convertValue(arguments, UserProfileSaveParams.class);
        McpCallContext ctx = context != null ? context : McpCallContext.http();
        return userProfileSaveService.save(params, ctx);
    }
}
