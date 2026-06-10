package com.neusoft.edu.neullmdev.mapper.classroom;

import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClassroomSubTaskMapper {

    @Insert("INSERT INTO classroom_sub_task (task_id, sub_task_id, order_no, title, description) "
            + "VALUES (#{taskId}, #{subTaskId}, #{orderNo}, #{title}, #{description})")
    int insert(ClassroomSubTaskEntity row);

    @Select("SELECT id, task_id AS taskId, sub_task_id AS subTaskId, order_no AS orderNo, title, description "
            + "FROM classroom_sub_task WHERE task_id = #{taskId} ORDER BY order_no")
    List<ClassroomSubTaskEntity> listByTask(@Param("taskId") String taskId);

    @Select("SELECT id, task_id AS taskId, sub_task_id AS subTaskId, order_no AS orderNo, title, description "
            + "FROM classroom_sub_task WHERE task_id = #{taskId} AND sub_task_id = #{subTaskId} LIMIT 1")
    ClassroomSubTaskEntity findOne(@Param("taskId") String taskId, @Param("subTaskId") String subTaskId);

    @Delete("DELETE FROM classroom_sub_task WHERE task_id = #{taskId}")
    int deleteByTask(@Param("taskId") String taskId);

    @Delete("DELETE FROM classroom_sub_task WHERE task_id = #{taskId} AND sub_task_id = #{subTaskId}")
    int deleteOne(@Param("taskId") String taskId, @Param("subTaskId") String subTaskId);

    @Update("UPDATE classroom_sub_task SET order_no = #{orderNo}, title = #{title}, description = #{description} "
            + "WHERE task_id = #{taskId} AND sub_task_id = #{subTaskId}")
    int updateContent(@Param("taskId") String taskId,
                      @Param("subTaskId") String subTaskId,
                      @Param("orderNo") int orderNo,
                      @Param("title") String title,
                      @Param("description") String description);
}
