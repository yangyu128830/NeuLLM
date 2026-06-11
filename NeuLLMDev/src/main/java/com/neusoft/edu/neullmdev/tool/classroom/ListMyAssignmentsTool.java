package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListMyAssignmentsTool implements McpToolHandler {

    private final ClassroomTaskService taskService;

    public ListMyAssignmentsTool(ClassroomTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String toolName() {
        return "list_my_assignments";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        var list = taskService.listMyAssignments();
        return new ToolResult(toolName(), "我的作业 " + list.size() + " 项", list);
    }
}
