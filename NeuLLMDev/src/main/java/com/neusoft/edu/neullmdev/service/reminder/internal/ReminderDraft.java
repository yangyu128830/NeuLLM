package com.neusoft.edu.neullmdev.service.reminder.internal;

/**
 * 学习提醒解析过程中的可变草稿（由 {@link com.neusoft.edu.neullmdev.service.reminder.internal.ReminderFieldResolver} 填充）。
 */
public final class ReminderDraft {

    private String eventName;
    private String eventDate;
    private String eventTime;
    private String phoneNumber;
    private String email;
    private String description;
    private String notifyMethod;

    public static ReminderDraft fromParams(
            String eventName,
            String eventDate,
            String eventTime,
            String phoneNumber,
            String email,
            String description,
            String notifyMethod) {
        ReminderDraft d = new ReminderDraft();
        d.eventName = eventName;
        d.eventDate = eventDate;
        d.eventTime = eventTime;
        d.phoneNumber = phoneNumber;
        d.email = email;
        d.description = description;
        d.notifyMethod = notifyMethod;
        return d;
    }

    public ReminderResolvedFields toResolved() {
        return new ReminderResolvedFields(
                eventName, eventDate, eventTime, phoneNumber, email, description, notifyMethod);
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotifyMethod() {
        return notifyMethod;
    }

    public void setNotifyMethod(String notifyMethod) {
        this.notifyMethod = notifyMethod;
    }
}
