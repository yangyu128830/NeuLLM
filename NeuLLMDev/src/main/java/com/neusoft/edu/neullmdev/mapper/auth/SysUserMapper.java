package com.neusoft.edu.neullmdev.mapper.auth;

import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {

    String USER_COLS = "id, username, password_hash AS passwordHash, role, display_name AS displayName, "
            + "student_no AS studentNo, class_id AS classId, major, grade, class_name AS className, "
            + "email, phone, taught_subjects AS taughtSubjects, teaching_scopes AS teachingScopes, "
            + "created_at AS createdAt, updated_at AS updatedAt ";

    @Select("SELECT " + USER_COLS + "FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUserEntity findByUsername(@Param("username") String username);

    @Select("SELECT " + USER_COLS + "FROM sys_user WHERE id = #{id}")
    SysUserEntity findById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM sys_user")
    long count();

    @Select("SELECT " + USER_COLS
            + "FROM sys_user WHERE role = 'STUDENT' AND class_id = #{classId} ORDER BY student_no, id")
    List<SysUserEntity> listStudentsByClass(@Param("classId") String classId);

    @Insert("INSERT INTO sys_user (username, password_hash, role, display_name, student_no, class_id, "
            + "major, grade, class_name, email, phone, taught_subjects, teaching_scopes) "
            + "VALUES (#{username}, #{passwordHash}, #{role}, #{displayName}, #{studentNo}, #{classId}, "
            + "#{major}, #{grade}, #{className}, #{email}, #{phone}, #{taughtSubjects}, #{teachingScopes})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUserEntity row);

    @Select("SELECT " + USER_COLS
            + "FROM sys_user WHERE role = 'STUDENT' AND class_id = #{classId} AND student_no = #{studentNo} LIMIT 1")
    SysUserEntity findStudentByClassAndNo(@Param("classId") String classId, @Param("studentNo") String studentNo);

    @Select("SELECT " + USER_COLS + "FROM sys_user WHERE student_no = #{studentNo} LIMIT 1")
    SysUserEntity findByStudentNo(@Param("studentNo") String studentNo);

    @Update("UPDATE sys_user SET username = #{username}, display_name = #{displayName}, "
            + "student_no = #{studentNo}, major = #{major}, grade = #{grade}, class_name = #{className}, "
            + "email = #{email}, phone = #{phone}, taught_subjects = #{taughtSubjects}, "
            + "teaching_scopes = #{teachingScopes}, "
            + "password_hash = #{passwordHash}, updated_at = NOW() WHERE id = #{id}")
    int update(SysUserEntity row);

    @Update("UPDATE sys_user SET username = #{username}, display_name = #{displayName}, "
            + "student_no = #{studentNo}, major = #{major}, grade = #{grade}, class_name = #{className}, "
            + "email = #{email}, phone = #{phone}, taught_subjects = #{taughtSubjects}, "
            + "teaching_scopes = #{teachingScopes}, "
            + "updated_at = NOW() WHERE id = #{id}")
    int updateWithoutPassword(SysUserEntity row);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
