package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderRequest;
import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderResponse;
import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;

public final class TravelReminderConverter {

    private TravelReminderConverter() {
    }

    public static TravelReminderEntity toEntity(TravelReminderRequest request) {
        if (request == null) {
            return new TravelReminderEntity();
        }
        TravelReminderEntity entity = new TravelReminderEntity();
        entity.setEventName(request.getEventName());
        entity.setEventDate(request.getEventDate());
        entity.setEventTime(request.getEventTime());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());
        entity.setDescription(request.getDescription());
        entity.setReminderMinutes(request.getReminderMinutes());
        return entity;
    }

    public static TravelReminderResponse toResponse(TravelReminderEntity entity) {
        if (entity == null) {
            return null;
        }
        TravelReminderResponse response = new TravelReminderResponse();
        response.setId(entity.getId());
        response.setEventName(entity.getEventName());
        response.setEventDate(entity.getEventDate());
        response.setEventTime(entity.getEventTime());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setEmail(entity.getEmail());
        response.setDescription(entity.getDescription());
        response.setReminderMinutes(entity.getReminderMinutes());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
