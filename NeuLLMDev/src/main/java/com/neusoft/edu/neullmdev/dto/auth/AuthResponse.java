package com.neusoft.edu.neullmdev.dto.auth;

import com.neusoft.edu.neullmdev.auth.AuthUser;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String role;
    private String displayName;
    private String studentNo;
    private String classId;

    public static AuthResponse of(String token, AuthUser user) {
        AuthResponse r = new AuthResponse();
        r.setToken(token);
        r.setUserId(user.id());
        r.setUsername(user.username());
        r.setRole(user.role().name());
        r.setDisplayName(user.displayName());
        r.setStudentNo(user.studentNo());
        r.setClassId(user.classId());
        return r;
    }
}
