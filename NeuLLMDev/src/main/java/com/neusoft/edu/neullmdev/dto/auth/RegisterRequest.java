package com.neusoft.edu.neullmdev.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "角色不能为空")
    private String role;
    @NotBlank(message = "显示名不能为空")
    private String displayName;
    private String studentNo;
    private String classId;
}
