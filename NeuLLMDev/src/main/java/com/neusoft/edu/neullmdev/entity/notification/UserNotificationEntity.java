package com.neusoft.edu.neullmdev.entity.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserNotificationEntity {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String linkPath;
    private String refId;
    private Boolean readFlag;
    private LocalDateTime createdAt;
}
