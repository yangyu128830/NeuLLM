package com.neusoft.edu.neullmdev.model.mcp;

/** MCP 工具调用结果：摘要供 Agent/用户阅读，data 供程序或前端卡片处理。 */
public class ToolResult {

    private String toolName;
    private String summary;
    private Object data;

    public ToolResult() {
    }

    public ToolResult(String toolName, String summary, Object data) {
        this.toolName = toolName;
        this.summary = summary;
        this.data = data;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
