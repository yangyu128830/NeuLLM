package com.neusoft.edu.neullmdev.dto.classroom.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentSubTaskResponse(
        String subTaskId,
        int orderNo,
        String title,
        String description,
        String submissionId,
        String status,
        String statusLabel,
        String fileName,
        LocalDateTime submittedAt,
        BigDecimal score,
        String teacherComment,
        boolean canSubmit
) {
}
