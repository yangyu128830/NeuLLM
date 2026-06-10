package com.neusoft.edu.neullmdev.mapper.classroom;

import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClassroomTaskMapper {

    String TASK_COLUMNS = "task_id AS taskId, class_id AS classId, teacher_id AS teacherId, title, description, "
            + "subject, target_major AS targetMajor, target_grade AS targetGrade, "
            + "target_class_name AS targetClassName, start_time AS startTime, end_time AS endTime, "
            + "published, created_at AS createdAt, updated_at AS updatedAt";

    @Insert("INSERT INTO classroom_task (task_id, class_id, teacher_id, title, description, subject, "
            + "target_major, target_grade, target_class_name, start_time, end_time, published) "
            + "VALUES (#{taskId}, #{classId}, #{teacherId}, #{title}, #{description}, #{subject}, "
            + "#{targetMajor}, #{targetGrade}, #{targetClassName}, #{startTime}, #{endTime}, #{published})")
    int insert(ClassroomTaskEntity row);

    @Select("SELECT " + TASK_COLUMNS + " FROM classroom_task WHERE task_id = #{taskId}")
    ClassroomTaskEntity findById(@Param("taskId") String taskId);

    @Select("SELECT " + TASK_COLUMNS + " FROM classroom_task "
            + "WHERE class_id = #{classId} ORDER BY created_at DESC")
    List<ClassroomTaskEntity> listByClass(@Param("classId") String classId);

    @Select("SELECT " + TASK_COLUMNS + " FROM classroom_task "
            + "WHERE class_id = #{classId} AND published = 1 ORDER BY created_at DESC")
    List<ClassroomTaskEntity> listPublishedByClass(@Param("classId") String classId);

    @Update("UPDATE classroom_task SET published = 1, updated_at = NOW() WHERE task_id = #{taskId}")
    int publish(@Param("taskId") String taskId);

    @Update("UPDATE classroom_task SET title = #{title}, description = #{description}, "
            + "subject = #{subject}, target_major = #{targetMajor}, target_grade = #{targetGrade}, "
            + "target_class_name = #{targetClassName}, start_time = #{startTime}, end_time = #{endTime}, "
            + "updated_at = NOW() WHERE task_id = #{taskId}")
    int updateMeta(ClassroomTaskEntity task);

    @Delete("DELETE FROM classroom_task WHERE task_id = #{taskId}")
    int delete(@Param("taskId") String taskId);

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(task_id, 6) AS UNSIGNED)), 0) FROM classroom_task WHERE task_id LIKE 'TASK-%'")
    int maxTaskSeq();
}
