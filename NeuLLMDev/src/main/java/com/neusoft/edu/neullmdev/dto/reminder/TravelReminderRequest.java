package com.neusoft.edu.neullmdev.dto.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** 管理页 CRUD 请求体（非 MCP 卡片确认）。 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelReminderRequest {
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String phoneNumber;
    private String email;
    private String description;
    private Integer reminderMinutes;
}
