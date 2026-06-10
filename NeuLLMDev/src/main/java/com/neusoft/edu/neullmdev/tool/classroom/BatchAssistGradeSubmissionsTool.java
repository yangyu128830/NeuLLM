package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomBatchGradingService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BatchAssistGradeSubmissionsTool implements McpToolHandler {

    private final ClassroomBatchGradingService batchGradingService;

    public BatchAssistGradeSubmissionsTool(ClassroomBatchGradingService batchGradingService) {
        this.batchGradingService = batchGradingService;
    }

    @Override
    public String toolName() {
        return "batch_assist_grade_submissions";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        String taskId = ListClassroomStudentsTool.stringArg(arguments, "taskId");
        Map<String, Object> data = batchGradingService.batchAssist(taskId);
        return new ToolResult(toolName(),
                "已批量生成 " + data.get("processed") + " 条 AI 批改建议（待批改共 "
                        + data.get("totalPending") + " 条）",
                data);
    }
}
