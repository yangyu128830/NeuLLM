package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PublishClassroomTaskTool implements McpToolHandler {

    private final ClassroomTaskService taskService;

    public PublishClassroomTaskTool(ClassroomTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String toolName() {
        return "publish_classroom_task";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        var task = taskService.publishTask(taskId);
        return new ToolResult(toolName(), "任务已发布：" + taskId, task);
    }
}
