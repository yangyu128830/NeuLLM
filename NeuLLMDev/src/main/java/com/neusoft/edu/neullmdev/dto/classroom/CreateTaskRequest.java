package com.neusoft.edu.neullmdev.dto.classroom;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private String subject;
    private String targetMajor;
    private String targetGrade;
    private String targetClassName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<SubTaskItem> subTasks;
    private Boolean publish;

    @Data
    public static class SubTaskItem {
        private String title;
        private String description;
    }
}
