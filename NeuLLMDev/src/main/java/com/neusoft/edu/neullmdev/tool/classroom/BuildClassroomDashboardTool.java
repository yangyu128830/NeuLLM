package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomDashboardService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuildClassroomDashboardTool implements McpToolHandler {

    private final ClassroomDashboardService dashboardService;

    public BuildClassroomDashboardTool(ClassroomDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public String toolName() {
        return "build_classroom_dashboard";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        ProgressDashboard dashboard = dashboardService.buildDashboard(taskId);
        return new ToolResult(toolName(), "已生成任务 " + taskId + " 进度看板", dashboard);
    }
}
