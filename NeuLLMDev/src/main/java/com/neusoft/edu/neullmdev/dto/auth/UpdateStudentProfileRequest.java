package com.neusoft.edu.neullmdev.dto.auth;

import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    private String displayName;
    private String username;
    /** 学号 */
    private String studentNo;
    /** 专业 */
    private String major;
    /** 年级，如 2024级 */
    private String grade;
    /** 班级名称，如 软工2401班 */
    private String className;
    private String email;
    private String phone;
    /** 留空则不修改密码 */
    private String password;
}
