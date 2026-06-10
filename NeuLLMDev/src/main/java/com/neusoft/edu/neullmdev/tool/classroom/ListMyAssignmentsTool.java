package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListMyAssignmentsTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public ListMyAssignmentsTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "list_my_assignments";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        var list = classroomService.listMyAssignments();
        return new ToolResult(toolName(), "共 " + list.size() + " 项已发布作业", list);
    }
}
