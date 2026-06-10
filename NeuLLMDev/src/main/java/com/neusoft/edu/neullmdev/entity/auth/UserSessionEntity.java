package com.neusoft.edu.neullmdev.entity.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSessionEntity {
    private String token;
    private Long userId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
