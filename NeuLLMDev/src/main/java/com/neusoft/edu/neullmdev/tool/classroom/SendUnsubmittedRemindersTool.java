package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomReminderService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SendUnsubmittedRemindersTool implements McpToolHandler {

    private final ClassroomReminderService reminderService;

    public SendUnsubmittedRemindersTool(ClassroomReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public String toolName() {
        return "send_unsubmitted_reminders";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        Map<String, Object> data = reminderService.sendReminders(taskId);
        int sent = (int) data.getOrDefault("sentCount", 0);
        int skipped = (int) data.getOrDefault("skippedCount", 0);
        int inApp = (int) data.getOrDefault("inAppCount", 0);
        return new ToolResult(toolName(),
                "已向 " + inApp + " 名学生发送站内催交消息，邮件成功 " + sent + " 封"
                        + (skipped > 0 ? "，" + skipped + " 人未配置邮箱" : ""),
                data);
    }
}
