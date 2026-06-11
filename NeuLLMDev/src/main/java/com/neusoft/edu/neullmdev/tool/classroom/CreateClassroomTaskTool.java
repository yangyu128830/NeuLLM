package com.neusoft.edu.neullmdev.tool.classroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateClassroomTaskTool implements McpToolHandler {

    private final ClassroomTaskService taskService;
    private final ObjectMapper objectMapper;

    public CreateClassroomTaskTool(ClassroomTaskService taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "create_classroom_task";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle(ListClassroomStudentsTool.stringArg(arguments, "title"));
        req.setDescription(ListClassroomStudentsTool.stringArg(arguments, "description"));
        Object publishArg = arguments.get("publish");
        boolean publish = publishArg instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(publishArg));
        req.setPublish(publish);
        Object subTasks = arguments.get("subTasks");
        if (subTasks instanceof List<?> list) {
            req.setSubTasks(list.stream()
                    .map(item -> objectMapper.convertValue(item, CreateTaskRequest.SubTaskItem.class))
                    .toList());
        }
        var task = taskService.createTask(req);
        return new ToolResult(toolName(), "已创建任务 " + task.taskId(), task);
    }
}
