package com.neusoft.edu.neullmdev.dto.auth;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTeacherProfileRequest {
    private String displayName;
    private String username;
    /** 工号 */
    private String employeeNo;
    /** 院系 */
    private String department;
    /** 职称 */
    private String title;
    private String email;
    private String phone;
    /** 留空则不修改密码 */
    private String password;
    /** 所教课程 */
    private List<String> taughtSubjects;
    /** 教学板块 */
    private List<TeachingScopeItem> teachingScopes;
}
