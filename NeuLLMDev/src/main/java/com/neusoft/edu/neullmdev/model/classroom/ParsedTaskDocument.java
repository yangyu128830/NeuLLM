package com.neusoft.edu.neullmdev.model.classroom;

import java.util.List;

public record ParsedTaskDocument(
        String title,
        String description,
        List<ParsedSubTask> subTasks,
        String sourceFileName,
        String preview
) {
}
