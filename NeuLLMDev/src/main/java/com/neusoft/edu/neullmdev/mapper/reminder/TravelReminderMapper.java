package com.neusoft.edu.neullmdev.mapper.reminder;

import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TravelReminderMapper {

    String COLUMNS =
            "id, event_name AS eventName, event_date AS eventDate, event_time AS eventTime, "
                    + "phone_number AS phoneNumber, email, description, reminder_minutes AS reminderMinutes, "
                    + "created_at AS createdAt, updated_at AS updatedAt";

    @Insert("INSERT INTO travel_reminder (id, event_name, event_date, event_time, phone_number, email, description, reminder_minutes, created_at, updated_at) "
            + "VALUES (#{id}, #{eventName}, #{eventDate}, #{eventTime}, #{phoneNumber}, #{email}, #{description}, #{reminderMinutes}, #{createdAt}, #{updatedAt})")
    int insert(TravelReminderEntity reminder);

    @Select("SELECT " + COLUMNS + " FROM travel_reminder ORDER BY event_date, event_time")
    List<TravelReminderEntity> selectAll();

    @Select("SELECT " + COLUMNS + " FROM travel_reminder WHERE id = #{id}")
    TravelReminderEntity selectById(String id);

    @Delete("DELETE FROM travel_reminder WHERE id = #{id}")
    int deleteById(String id);

    @Update("UPDATE travel_reminder SET event_name = #{eventName}, event_date = #{eventDate}, event_time = #{eventTime}, "
            + "phone_number = #{phoneNumber}, email = #{email}, description = #{description}, reminder_minutes = #{reminderMinutes}, updated_at = #{updatedAt} "
            + "WHERE id = #{id}")
    int update(TravelReminderEntity reminder);
}
