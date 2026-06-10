package com.neusoft.edu.neullmdev.mapper.notification;

import com.neusoft.edu.neullmdev.entity.notification.UserNotificationEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserNotificationMapper {

    @Insert("INSERT INTO user_notification (user_id, type, title, content, link_path, ref_id, read_flag, created_at) "
            + "VALUES (#{userId}, #{type}, #{title}, #{content}, #{linkPath}, #{refId}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotificationEntity row);

    @Select("SELECT id, user_id AS userId, type, title, content, link_path AS linkPath, ref_id AS refId, "
            + "read_flag AS readFlag, created_at AS createdAt "
            + "FROM user_notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<UserNotificationEntity> listByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM user_notification WHERE user_id = #{userId} AND read_flag = 0")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE user_notification SET read_flag = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE user_notification SET read_flag = 1 WHERE user_id = #{userId} AND read_flag = 0")
    int markAllRead(@Param("userId") Long userId);

    @Delete("DELETE FROM user_notification WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
