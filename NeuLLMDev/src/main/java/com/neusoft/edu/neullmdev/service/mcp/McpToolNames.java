package com.neusoft.edu.neullmdev.service.mcp;

/** 工具名规范化：模型别名与历史函数名映射到 MCP 标准名。 */
public final class McpToolNames {

    private McpToolNames() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String n = raw.toLowerCase().trim();
        return switch (n) {
            case "settravelreminder", "set_travel_reminder" -> "create_travel_reminder";
            case "wordwrite" -> "word_write";
            case "getlocationinfo" -> "get_location_info";
            default -> n;
        };
    }
}
