package com.neusoft.edu.neullmdev.mapper.classroom;

import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClassroomSubmissionMapper {

    @Insert("INSERT INTO classroom_submission (submission_id, task_id, sub_task_id, student_user_id, file_name, "
            + "content, artifact_path, status, submitted_at) "
            + "VALUES (#{submissionId}, #{taskId}, #{subTaskId}, #{studentUserId}, #{fileName}, "
            + "#{content}, #{artifactPath}, #{status}, #{submittedAt})")
    int insert(ClassroomSubmissionEntity row);

    @Update("UPDATE classroom_submission SET file_name=#{fileName}, content=#{content}, artifact_path=#{artifactPath}, "
            + "status='SUBMITTED', score=NULL, teacher_comment=NULL, graded_by=NULL, graded_at=NULL, "
            + "submitted_at=NOW() WHERE submission_id=#{submissionId}")
    int resubmit(ClassroomSubmissionEntity row);

    @Select("SELECT submission_id AS submissionId, task_id AS taskId, sub_task_id AS subTaskId, "
            + "student_user_id AS studentUserId, file_name AS fileName, content, artifact_path AS artifactPath, "
            + "status, score, teacher_comment AS teacherComment, graded_by AS gradedBy, graded_at AS gradedAt, "
            + "submitted_at AS submittedAt FROM classroom_submission WHERE submission_id = #{submissionId}")
    ClassroomSubmissionEntity findById(@Param("submissionId") String submissionId);

    @Select("SELECT submission_id AS submissionId, task_id AS taskId, sub_task_id AS subTaskId, "
            + "student_user_id AS studentUserId, file_name AS fileName, content, artifact_path AS artifactPath, "
            + "status, score, teacher_comment AS teacherComment, graded_by AS gradedBy, graded_at AS gradedAt, "
            + "submitted_at AS submittedAt FROM classroom_submission "
            + "WHERE task_id = #{taskId} AND sub_task_id = #{subTaskId} AND student_user_id = #{studentUserId} LIMIT 1")
    ClassroomSubmissionEntity findCell(@Param("taskId") String taskId,
                                       @Param("subTaskId") String subTaskId,
                                       @Param("studentUserId") Long studentUserId);

    @Select("SELECT submission_id AS submissionId, task_id AS taskId, sub_task_id AS subTaskId, "
            + "student_user_id AS studentUserId, file_name AS fileName, content, artifact_path AS artifactPath, "
            + "status, score, teacher_comment AS teacherComment, graded_by AS gradedBy, graded_at AS gradedAt, "
            + "submitted_at AS submittedAt FROM classroom_submission WHERE task_id = #{taskId} ORDER BY submitted_at")
    List<ClassroomSubmissionEntity> listByTask(@Param("taskId") String taskId);

    @Update("UPDATE classroom_submission SET status='GRADED', score=#{score}, teacher_comment=#{teacherComment}, "
            + "graded_by=#{gradedBy}, graded_at=NOW() WHERE submission_id=#{submissionId}")
    int grade(@Param("submissionId") String submissionId,
              @Param("score") java.math.BigDecimal score,
              @Param("teacherComment") String teacherComment,
              @Param("gradedBy") Long gradedBy);

    @Update("UPDATE classroom_submission SET status='REJECTED', teacher_comment=#{teacherComment}, "
            + "graded_by=#{gradedBy}, graded_at=NOW(), score=NULL WHERE submission_id=#{submissionId}")
    int reject(@Param("submissionId") String submissionId,
               @Param("teacherComment") String teacherComment,
               @Param("gradedBy") Long gradedBy);

    @Delete("DELETE FROM classroom_submission WHERE task_id = #{taskId}")
    int deleteByTask(@Param("taskId") String taskId);

    @Delete("DELETE FROM classroom_submission WHERE student_user_id = #{studentUserId}")
    int deleteByStudentUserId(@Param("studentUserId") Long studentUserId);

    @Select("SELECT COUNT(*) FROM classroom_submission WHERE task_id = #{taskId} AND sub_task_id = #{subTaskId}")
    int countBySubTask(@Param("taskId") String taskId, @Param("subTaskId") String subTaskId);

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(submission_id, 9) AS UNSIGNED)), 0) FROM classroom_submission "
            + "WHERE submission_id LIKE 'SUBMIT-%'")
    int maxSubmissionSeq();
}
