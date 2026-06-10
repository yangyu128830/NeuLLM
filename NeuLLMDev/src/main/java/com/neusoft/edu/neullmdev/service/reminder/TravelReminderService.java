package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;

import java.util.List;

public interface TravelReminderService {
    TravelReminderEntity save(TravelReminderEntity reminder);
    List<TravelReminderEntity> findAll();
    TravelReminderEntity findById(String id);
    void deleteById(String id);
    TravelReminderEntity update(TravelReminderEntity reminder);
}