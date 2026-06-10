package com.neusoft.edu.neullmdev.dto.reminder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelReminderParams {
    @JsonAlias({"eventName", "event_name", "title", "reminderName", "reminder_name", "type"})
    private String eventName;
    private Object events;
    @JsonAlias({"eventDate", "event_date", "date", "reminderDate", "reminder_date", "checkInDate"})
    private String eventDate;
    @JsonAlias({"eventTime", "event_time", "time", "reminderTime", "reminder_time", "checkOutTime"})
    private String eventTime;
    @JsonAlias({"phoneNumber", "phone_number", "phone", "mobile", "mobile_number", "number"})
    private String phoneNumber;
    @JsonAlias({"email", "emailAddress", "recipient", "to"})
    private String email;
    @JsonAlias({"description", "detail", "note", "notes", "message", "content"})
    private String description;

    @JsonAlias({"datetime", "dateTime", "scheduledAt", "when", "startTime"})
    private String datetime;

    @JsonAlias({"advanceNotice", "advance_notice", "minutesBefore", "minutes_before"})
    private String advanceNotice;

    private Object remindTime;
    private String notifyMethod;
}
