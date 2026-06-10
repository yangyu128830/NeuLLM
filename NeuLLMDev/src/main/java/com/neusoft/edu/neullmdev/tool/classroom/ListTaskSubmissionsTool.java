package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListTaskSubmissionsTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public ListTaskSubmissionsTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "list_task_submissions";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        var list = classroomService.listSubmissions(taskId);
        return new ToolResult(toolName(), "任务 " + taskId + " 共 " + list.size() + " 条提交", list);
    }
}
