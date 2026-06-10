package com.neusoft.edu.neullmdev.dto.classroom;

import lombok.Data;

@Data
public class GradeSubmissionRequest {
    private String submissionId;
    private Double score;
    private String comment;
}
