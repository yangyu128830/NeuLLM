package com.neusoft.edu.neullmdev.entity.classroom;

import lombok.Data;

@Data
public class ClassroomSubTaskEntity {
    private Long id;
    private String taskId;
    private String subTaskId;
    private Integer orderNo;
    private String title;
    private String description;
}
