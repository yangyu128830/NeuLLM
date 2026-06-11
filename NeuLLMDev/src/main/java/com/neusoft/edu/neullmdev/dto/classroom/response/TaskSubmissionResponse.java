package com.neusoft.edu.neullmdev.dto.classroom.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskSubmissionResponse(
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
        LocalDateTime gradedAt,
        String studentName,
        String studentNo
) {
    public static TaskSubmissionResponse from(SubmissionResponse submission, String studentName, String studentNo) {
        return new TaskSubmissionResponse(
                submission.submissionId(),
                submission.taskId(),
                submission.subTaskId(),
                submission.studentUserId(),
                submission.fileName(),
                submission.content(),
                submission.status(),
                submission.score(),
                submission.teacherComment(),
                submission.submittedAt(),
                submission.gradedAt(),
                studentName,
                studentNo);
    }
}
