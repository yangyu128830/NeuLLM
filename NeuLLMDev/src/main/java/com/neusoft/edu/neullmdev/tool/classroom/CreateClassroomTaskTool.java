package com.neusoft.edu.neullmdev.tool.classroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateClassroomTaskTool implements McpToolHandler {

    private final ClassroomService classroomService;
    private final ObjectMapper objectMapper;

    public CreateClassroomTaskTool(ClassroomService classroomService, ObjectMapper objectMapper) {
        this.classroomService = classroomService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "create_classroom_task";
    }

    @Override
    @SuppressWarnings("unchecked")
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
        Map<String, Object> task = classroomService.createTask(req);
        return new ToolResult(toolName(), "已创建任务 " + task.get("taskId"), task);
    }
}
