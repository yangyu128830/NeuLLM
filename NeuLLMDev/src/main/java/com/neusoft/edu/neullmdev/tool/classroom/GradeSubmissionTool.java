package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GradeSubmissionTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public GradeSubmissionTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "grade_submission";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String submissionId = ListClassroomStudentsTool.stringArg(arguments, "submissionId");
        double score = Double.parseDouble(String.valueOf(arguments.get("score")));
        String comment = ListClassroomStudentsTool.stringArg(arguments, "comment");
        Map<String, Object> result = classroomService.gradeSubmission(submissionId, score, comment);
        return new ToolResult(toolName(), "批改完成，得分 " + score, result);
    }
}
