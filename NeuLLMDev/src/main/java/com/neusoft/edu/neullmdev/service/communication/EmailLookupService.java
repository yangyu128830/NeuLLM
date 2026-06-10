package com.neusoft.edu.neullmdev.service.communication;

import com.neusoft.edu.neullmdev.dto.communication.EmailLookupParams;
import com.neusoft.edu.neullmdev.entity.communication.EmailAddressEntity;
import com.neusoft.edu.neullmdev.mapper.communication.EmailAddressMapper;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class EmailLookupService {

    private final EmailAddressMapper emailAddressMapper;

    public EmailLookupService(EmailAddressMapper emailAddressMapper) {
        this.emailAddressMapper = emailAddressMapper;
    }

    public EmailAddressEntity findByAccountName(String account) {
        if (account == null || account.isBlank()) {
            return null;
        }
        return emailAddressMapper.selectByEmailName(account.trim());
    }

    public ToolResult lookup(EmailLookupParams params) {
        return McpToolSupport.fromJsonString("get_email_info", buildJson(params));
    }

    public String buildJson(EmailLookupParams params) {
        JSONObject result = new JSONObject();
        result.put("functionName", "get_email_info");
        if (params == null || params.getAccount() == null || params.getAccount().isBlank()) {
            result.put("ok", false);
            result.put("message", "请提供 account（联系人别名）");
            return result.toString();
        }
        EmailAddressEntity row = findByAccountName(params.getAccount());
        if (row == null) {
            result.put("ok", false);
            result.put("message", "未在 emailaddress 表中找到别名: " + params.getAccount().trim());
            return result.toString();
        }
        result.put("ok", true);
        result.put("account", row.getEmailName());
        result.put("recipient", row.getAddress());
        result.put("email", row.getAddress());
        return result.toString();
    }
}