package com.neusoft.edu.neullmdev.dto.classroom.response;

import java.time.LocalDateTime;
import java.util.List;

public record ClassroomTaskResponse(
        String taskId,
        String classId,
        String title,
        String description,
        String subject,
        String targetMajor,
        String targetGrade,
        String targetClassName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean published,
        List<SubTaskResponse> subTasks
) {
}
