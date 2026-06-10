package com.neusoft.edu.neullmdev.model.mcp;

/**
 * Agent 对话阶段的调用上下文（教学版 MCP HTTP 调用通常不需要）。
 *
 * @param userInput         用户原始输入，部分工具（如旅行提醒）用于补全字段
 * @param commitSideEffects false 时仅返回卡片 JSON，不执行发邮件/写库等副作用
 */
public record McpCallContext(String userInput, boolean commitSideEffects) {

    public static McpCallContext preview(String userInput) {
        return new McpCallContext(userInput != null ? userInput : "", false);
    }

    public static McpCallContext committed(String userInput) {
        return new McpCallContext(userInput != null ? userInput : "", true);
    }

    public static McpCallContext http() {
        return new McpCallContext("", true);
    }
}
