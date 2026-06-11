package com.neusoft.edu.neullmdev.dto.classroom.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubmissionResponse(
        String submissionId,
        String taskId,
        String subTaskId,
        Long studentUserId,
        String fileName,
        String content,
        String status,
        BigDecimal score,
        String teacherComment,
        LocalDateTime submittedAt,
        LocalDateTime gradedAt
) {
}
