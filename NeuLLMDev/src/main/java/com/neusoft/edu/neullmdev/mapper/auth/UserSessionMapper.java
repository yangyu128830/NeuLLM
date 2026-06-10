package com.neusoft.edu.neullmdev.mapper.auth;

import com.neusoft.edu.neullmdev.entity.auth.UserSessionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserSessionMapper {

    @Insert("INSERT INTO user_session (token, user_id, expires_at) VALUES (#{token}, #{userId}, #{expiresAt})")
    int insert(UserSessionEntity row);

    @Select("SELECT token, user_id AS userId, expires_at AS expiresAt, created_at AS createdAt "
            + "FROM user_session WHERE token = #{token}")
    UserSessionEntity findByToken(@Param("token") String token);

    @Delete("DELETE FROM user_session WHERE token = #{token}")
    int deleteByToken(@Param("token") String token);

    @Delete("DELETE FROM user_session WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
