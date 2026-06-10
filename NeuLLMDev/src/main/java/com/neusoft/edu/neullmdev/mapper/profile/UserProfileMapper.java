package com.neusoft.edu.neullmdev.mapper.profile;

import com.neusoft.edu.neullmdev.entity.profile.UserProfileEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper {

    @Insert("INSERT INTO user_profile (external_id, display_name, real_name, email, phone, address, gender, avatar_url, remark, created_at, updated_at) "
            + "VALUES (#{externalId}, #{displayName}, #{realName}, #{email}, #{phone}, #{address}, #{gender}, #{avatarUrl}, #{remark}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProfileEntity row);

    /** 按真实姓名或显示名匹配，取最新一条（用于发邮件前预填收件人） */
    @Select("SELECT id, external_id AS externalId, display_name AS displayName, real_name AS realName, "
            + "email, phone, address, gender, avatar_url AS avatarUrl, remark, created_at AS createdAt, updated_at AS updatedAt "
            + "FROM user_profile WHERE real_name = #{name} OR display_name = #{name} "
            + "ORDER BY id DESC LIMIT 1")
    UserProfileEntity findLatestByRealOrDisplayName(@Param("name") String name);
}
