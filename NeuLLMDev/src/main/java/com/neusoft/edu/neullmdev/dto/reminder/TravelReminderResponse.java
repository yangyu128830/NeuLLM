package com.neusoft.edu.neullmdev.dto.reminder;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TravelReminderResponse {
    private String id;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String phoneNumber;
    private String email;
    private String description;
    private Integer reminderMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
