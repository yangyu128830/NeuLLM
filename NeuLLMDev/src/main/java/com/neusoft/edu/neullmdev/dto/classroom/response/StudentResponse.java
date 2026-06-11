package com.neusoft.edu.neullmdev.dto.classroom.response;

public record StudentResponse(
        String studentId,
        Long studentUserId,
        String name,
        String username,
        String classId,
        String major,
        String grade,
        String className
) {
}
