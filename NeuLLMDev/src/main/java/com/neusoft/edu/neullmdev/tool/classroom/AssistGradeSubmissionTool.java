package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.SubmissionGradingAssistService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP：AI 总结学生提交并给出批改建议（分数、评语、是否建议打回）。
 */
@Component
public class AssistGradeSubmissionTool implements McpToolHandler {

    private final SubmissionGradingAssistService assistService;

    public AssistGradeSubmissionTool(SubmissionGradingAssistService assistService) {
        this.assistService = assistService;
    }

    @Override
    public String toolName() {
        return "assist_grade_submission";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String submissionId = ListClassroomStudentsTool.stringArg(arguments, "submissionId");
        Map<String, Object> result = assistService.assist(submissionId);
        Object score = result.get("suggestedScore");
        String summary = String.valueOf(result.getOrDefault("summary", ""));
        String tip = "AI 建议分数 " + score + "；" + (summary.length() > 80 ? summary.substring(0, 80) + "…" : summary);
        return new ToolResult(toolName(), tip, result);
    }
}
