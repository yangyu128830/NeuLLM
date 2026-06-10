package com.neusoft.edu.neullmdev.model.classroom;

import java.util.List;
import java.util.Map;

public record ProgressDashboard(
        String taskId,
        String title,
        List<Map<String, Object>> subTasks,
        List<Map<String, Object>> rows,
        Map<String, Object> summary
) {
}
