package com.neusoft.edu.neullmdev.entity.classroom;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClassroomSubmissionEntity {
    private String submissionId;
    private String taskId;
    private String subTaskId;
    private Long studentUserId;
    private String fileName;
    private String content;
    private String artifactPath;
    private String status;
    private BigDecimal score;
    private String teacherComment;
    private Long gradedBy;
    private LocalDateTime gradedAt;
    private LocalDateTime submittedAt;
}
