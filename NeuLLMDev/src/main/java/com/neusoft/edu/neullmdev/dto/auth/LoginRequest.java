package com.neusoft.edu.neullmdev.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    /** 可选：STUDENT / TEACHER，与所选登录端一致时方可登录 */
    private String role;
}
