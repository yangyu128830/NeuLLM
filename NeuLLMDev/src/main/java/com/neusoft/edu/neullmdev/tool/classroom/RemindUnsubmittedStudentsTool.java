package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomReminderService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RemindUnsubmittedStudentsTool implements McpToolHandler {

    private final ClassroomReminderService reminderService;

    public RemindUnsubmittedStudentsTool(ClassroomReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public String toolName() {
        return "remind_unsubmitted_students";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        Map<String, Object> data = reminderService.buildReminder(taskId);
        int n = (int) data.getOrDefault("pendingCount", 0);
        return new ToolResult(toolName(), "已生成催交名单与话术，待提交 " + n + " 人", data);
    }
}
