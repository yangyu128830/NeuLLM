package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomSubmissionService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubmitAssignmentTool implements McpToolHandler {

    private final ClassroomSubmissionService submissionService;

    public SubmitAssignmentTool(ClassroomSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    public String toolName() {
        return "submit_assignment";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        String subTaskId = ListClassroomStudentsTool.stringArg(arguments, "subTaskId");
        String fileName = ListClassroomStudentsTool.stringArg(arguments, "fileName");
        String content = ListClassroomStudentsTool.stringArg(arguments, "content");
        var result = submissionService.submitText(taskId, subTaskId, fileName, content);
        return new ToolResult(toolName(), "提交成功：" + result.submissionId(), result);
    }
}
