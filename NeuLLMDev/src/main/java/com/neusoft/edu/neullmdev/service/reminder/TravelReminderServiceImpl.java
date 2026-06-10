package com.neusoft.edu.neullmdev.service.reminder;

import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import com.neusoft.edu.neullmdev.mapper.reminder.TravelReminderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TravelReminderServiceImpl implements TravelReminderService {

    private final TravelReminderMapper travelReminderMapper;

    public TravelReminderServiceImpl(TravelReminderMapper travelReminderMapper) {
        this.travelReminderMapper = travelReminderMapper;
    }

    @Override
    public TravelReminderEntity save(TravelReminderEntity reminder) {
        reminder.setId(UUID.randomUUID().toString());
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());
        if (reminder.getReminderMinutes() == null) {
            reminder.setReminderMinutes(15);
        }
        travelReminderMapper.insert(reminder);
        return reminder;
    }

    @Override
    public List<TravelReminderEntity> findAll() {
        return travelReminderMapper.selectAll();
    }

    @Override
    public TravelReminderEntity findById(String id) {
        return travelReminderMapper.selectById(id);
    }

    @Override
    public void deleteById(String id) {
        travelReminderMapper.deleteById(id);
    }

    @Override
    public TravelReminderEntity update(TravelReminderEntity reminder) {
        reminder.setUpdatedAt(LocalDateTime.now());
        travelReminderMapper.update(reminder);
        return reminder;
    }
}
