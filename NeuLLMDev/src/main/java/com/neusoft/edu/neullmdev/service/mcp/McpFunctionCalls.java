package com.neusoft.edu.neullmdev.service.mcp;

import java.util.LinkedHashMap;
import java.util.Map;

/** 构造经 {@link McpService#callTool} 使用的参数 Map。 */
public final class McpFunctionCalls {

    private McpFunctionCalls() {
    }

    public static Map<String, Object> sendEmailArgs(String recipient, String subject, String content) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("recipient", recipient != null ? recipient : "");
        params.put("subject", subject != null ? subject : "");
        params.put("content", content != null ? content : "");
        return params;
    }
}
