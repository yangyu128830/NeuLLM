package com.neusoft.edu.neullmdev.auth;

public enum UserRole {
    STUDENT,
    TEACHER;

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("角色不能为空");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
