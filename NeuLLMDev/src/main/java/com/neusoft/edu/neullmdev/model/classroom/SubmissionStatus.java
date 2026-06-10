package com.neusoft.edu.neullmdev.model.classroom;

public enum SubmissionStatus {
    SUBMITTED,
    GRADED,
    REJECTED;

    public static SubmissionStatus from(String value) {
        return value == null ? SUBMITTED : SubmissionStatus.valueOf(value);
    }
}
