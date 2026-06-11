package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomSubmissionService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GradeSubmissionTool implements McpToolHandler {

    private final ClassroomSubmissionService submissionService;

    public GradeSubmissionTool(ClassroomSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    public String toolName() {
        return "grade_submission";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String submissionId = ListClassroomStudentsTool.stringArg(arguments, "submissionId");
        double score = doubleArg(arguments, "score", 0);
        String comment = ListClassroomStudentsTool.stringArg(arguments, "comment");
        var result = submissionService.gradeSubmission(submissionId, score, comment);
        return new ToolResult(toolName(), "已评分 " + score + " 分", result);
    }

    private static double doubleArg(Map<String, Object> arguments, String key, double def) {
        if (arguments == null) {
            return def;
        }
        Object v = arguments.get(key);
        if (v == null) {
            return def;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
