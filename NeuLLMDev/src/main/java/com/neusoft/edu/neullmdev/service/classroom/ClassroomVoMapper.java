package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentRosterEntry;
import com.neusoft.edu.neullmdev.dto.classroom.response.SubTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.SubmissionResponse;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;

import java.util.List;

public final class ClassroomVoMapper {

    private ClassroomVoMapper() {
    }

    public static StudentResponse toStudent(SysUserEntity s) {
        return new StudentResponse(
                s.getStudentNo(),
                s.getId(),
                s.getDisplayName(),
                s.getUsername(),
                s.getClassId(),
                s.getMajor(),
                s.getGrade(),
                s.getClassName());
    }

    public static StudentRosterEntry toRosterEntry(SysUserEntity s) {
        return new StudentRosterEntry(s.getMajor(), s.getGrade(), s.getClassName());
    }

    public static SubTaskResponse toSubTask(ClassroomSubTaskEntity s) {
        return new SubTaskResponse(
                s.getSubTaskId(),
                s.getOrderNo(),
                s.getTitle(),
                s.getDescription());
    }

    public static ClassroomTaskResponse toTask(ClassroomTaskEntity task, List<ClassroomSubTaskEntity> subTasks) {
        return new ClassroomTaskResponse(
                task.getTaskId(),
                task.getClassId(),
                task.getTitle(),
                task.getDescription(),
                task.getSubject(),
                task.getTargetMajor(),
                task.getTargetGrade(),
                task.getTargetClassName(),
                task.getStartTime(),
                task.getEndTime(),
                Boolean.TRUE.equals(task.getPublished()),
                subTasks.stream().map(ClassroomVoMapper::toSubTask).toList());
    }

    public static SubmissionResponse toSubmission(ClassroomSubmissionEntity s) {
        return new SubmissionResponse(
                s.getSubmissionId(),
                s.getTaskId(),
                s.getSubTaskId(),
                s.getStudentUserId(),
                s.getFileName(),
                s.getContent(),
                s.getStatus(),
                s.getScore(),
                s.getTeacherComment(),
                s.getSubmittedAt(),
                s.getGradedAt());
    }
}
