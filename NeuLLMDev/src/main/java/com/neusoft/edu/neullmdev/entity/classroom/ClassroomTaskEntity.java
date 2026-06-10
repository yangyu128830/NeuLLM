package com.neusoft.edu.neullmdev.entity.classroom;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassroomTaskEntity {
    private String taskId;
    private String classId;
    private Long teacherId;
    private String title;
    private String description;
    private String subject;
    private String targetMajor;
    private String targetGrade;
    private String targetClassName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
