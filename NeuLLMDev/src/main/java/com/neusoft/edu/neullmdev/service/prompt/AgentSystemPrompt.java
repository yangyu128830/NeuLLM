package com.neusoft.edu.neullmdev.service.prompt;

import com.neusoft.edu.neullmdev.service.mcp.McpToolCatalog;
import org.springframework.stereotype.Component;

/** 拼装发给 Kimi 的系统提示：人设 + 意图规则 + 工具列表（来自 {@link McpToolCatalog}）。 */
@Component
public class AgentSystemPrompt {

    private final McpToolCatalog mcpToolCatalog;

    public AgentSystemPrompt(McpToolCatalog mcpToolCatalog) {
        this.mcpToolCatalog = mcpToolCatalog;
    }

    public String getPrompt() {
        return getPrompt(false);
    }

    public String getPrompt(boolean teacherMode) {
        if (teacherMode) {
            return TeacherAgentPersonaSection.text().trim()
                    + "\n\n"
                    + TeacherAgentIntentRulesSection.text().trim()
                    + "\n\n"
                    + mcpToolCatalog.renderTeacherPromptToolList();
        }
        return AgentPersonaSection.text().trim()
                + "\n\n"
                + AgentIntentRulesSection.text().trim()
                + "\n\n"
                + mcpToolCatalog.renderPromptToolList();
    }
}
