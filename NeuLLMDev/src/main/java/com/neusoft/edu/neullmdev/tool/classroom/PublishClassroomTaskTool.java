package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PublishClassroomTaskTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public PublishClassroomTaskTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "publish_classroom_task";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        Map<String, Object> task = classroomService.publishTask(taskId);
        return new ToolResult(toolName(), "任务已发布：" + taskId, task);
    }
}
