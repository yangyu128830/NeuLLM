package com.neusoft.edu.neullmdev.entity.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserEntity {
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String displayName;
    private String studentNo;
    private String classId;
    /** 专业 */
    private String major;
    /** 年级，如 2024级 */
    private String grade;
    /** 班级名称，如 软工2401班 */
    private String className;
    private String email;
    private String phone;
    /** 教师所教课程 JSON 数组 */
    private String taughtSubjects;
    /** 教师教学板块 JSON 数组 [{major,grade,className}] */
    private String teachingScopes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
