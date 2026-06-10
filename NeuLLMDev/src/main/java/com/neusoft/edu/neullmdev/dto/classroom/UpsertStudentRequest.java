package com.neusoft.edu.neullmdev.dto.classroom;

import lombok.Data;

@Data
public class UpsertStudentRequest {
    private String username;
    private String displayName;
    private String studentNo;
    /** 专业 */
    private String major;
    /** 年级，如 2024级 */
    private String grade;
    /** 班级名称，如 软工2401班 */
    private String className;
    /** 新建必填；更新时留空则不改密码 */
    private String password;
}
