package com.neusoft.edu.neullmdev.model.mcp;

import java.util.Map;

/** MCP 工具元数据（与教学模板 mcp-server-starter 一致）。 */
public class McpTool {

    private String name;
    private String description;
    private Map<String, Object> inputSchema;

    public McpTool() {
    }

    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
}
