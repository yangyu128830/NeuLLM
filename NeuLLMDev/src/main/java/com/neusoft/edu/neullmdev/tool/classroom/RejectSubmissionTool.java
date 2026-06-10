package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RejectSubmissionTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public RejectSubmissionTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "reject_submission";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String submissionId = ListClassroomStudentsTool.stringArg(arguments, "submissionId");
        String comment = ListClassroomStudentsTool.stringArg(arguments, "comment");
        Map<String, Object> result = classroomService.rejectSubmission(submissionId, comment);
        return new ToolResult(toolName(), "已打回，学生可重新提交", result);
    }
}
