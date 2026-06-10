package com.neusoft.edu.neullmdev.controller.auth;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.config.web.AuthInterceptor;
import com.neusoft.edu.neullmdev.dto.auth.AuthResponse;
import com.neusoft.edu.neullmdev.dto.auth.LoginRequest;
import com.neusoft.edu.neullmdev.dto.auth.RegisterRequest;
import com.neusoft.edu.neullmdev.dto.auth.UpdateStudentProfileRequest;
import com.neusoft.edu.neullmdev.dto.auth.UpdateTeacherProfileRequest;
import com.neusoft.edu.neullmdev.model.api.ApiResponse;
import com.neusoft.edu.neullmdev.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(AuthInterceptor.extractToken(request));
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> me() {
        AuthUser user = AuthContext.require();
        AuthResponse r = new AuthResponse();
        r.setUserId(user.id());
        r.setUsername(user.username());
        r.setRole(user.role().name());
        r.setDisplayName(user.displayName());
        r.setStudentNo(user.studentNo());
        r.setClassId(user.classId());
        return ApiResponse.success(r);
    }

    @GetMapping("/teacher/profile")
    public ApiResponse<Map<String, Object>> teacherProfile() {
        return ApiResponse.success(authService.getTeacherProfile());
    }

    @PutMapping("/teacher/profile")
    public ApiResponse<Map<String, Object>> updateTeacherProfile(@RequestBody UpdateTeacherProfileRequest request) {
        return ApiResponse.success(authService.updateTeacherProfile(request));
    }

    @GetMapping("/student/profile")
    public ApiResponse<Map<String, Object>> studentProfile() {
        return ApiResponse.success(authService.getStudentProfile());
    }

    @PutMapping("/student/profile")
    public ApiResponse<Map<String, Object>> updateStudentProfile(@RequestBody UpdateStudentProfileRequest request) {
        return ApiResponse.success(authService.updateStudentProfile(request));
    }
}
