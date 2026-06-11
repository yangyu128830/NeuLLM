package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.dto.auth.TeachingScopeItem;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomTaskMapper;
import com.neusoft.edu.neullmdev.service.auth.TeacherTeachingScopeUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClassroomAccessSupport {

    private final ClassroomTaskMapper taskMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final ClassroomSubmissionMapper submissionMapper;
    private final SysUserMapper sysUserMapper;

    public ClassroomAccessSupport(ClassroomTaskMapper taskMapper,
                                  ClassroomSubTaskMapper subTaskMapper,
                                  ClassroomSubmissionMapper submissionMapper,
                                  SysUserMapper sysUserMapper) {
        this.taskMapper = taskMapper;
        this.subTaskMapper = subTaskMapper;
        this.submissionMapper = submissionMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public void requireTeacher() {
        if (AuthContext.require().role() != UserRole.TEACHER) {
            throw new IllegalArgumentException("仅教师可执行此操作");
        }
    }

    public void requireStudent() {
        if (AuthContext.require().role() != UserRole.STUDENT) {
            throw new IllegalArgumentException("仅学生可执行此操作");
        }
    }

    public SysUserMapper sysUserMapper() {
        return sysUserMapper;
    }

    public ClassroomTaskMapper taskMapper() {
        return taskMapper;
    }

    public ClassroomSubTaskMapper subTaskMapper() {
        return subTaskMapper;
    }

    public ClassroomSubmissionMapper submissionMapper() {
        return submissionMapper;
    }

    public SysUserEntity requireClassStudent(Long studentUserId) {
        if (studentUserId == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        SysUserEntity student = sysUserMapper.findById(studentUserId);
        if (student == null || !UserRole.STUDENT.name().equals(student.getRole())) {
            throw new IllegalArgumentException("学生不存在");
        }
        String classId = AuthContext.require().classId();
        if (classId != null && !classId.isBlank() && student.getClassId() != null
                && !classId.equals(student.getClassId())) {
            throw new IllegalArgumentException("无权操作其他班级学生");
        }
        if (!TeacherTeachingScopeUtil.studentAccessible(student, currentTeacherScopes())) {
            throw new IllegalArgumentException("无权操作该教学板块外的学生");
        }
        return student;
    }

    public List<TeachingScopeItem> currentTeacherScopes() {
        SysUserEntity teacher = sysUserMapper.findById(AuthContext.require().id());
        if (teacher == null) {
            return List.of();
        }
        return TeacherTeachingScopeUtil.parseScopes(teacher.getTeachingScopes());
    }

    public List<SysUserEntity> listAccessibleStudents(String classId) {
        String cid = classId == null || classId.isBlank() ? AuthContext.require().classId() : classId;
        List<TeachingScopeItem> scopes = currentTeacherScopes();
        List<SysUserEntity> all = sysUserMapper.listStudentsByClass(cid);
        if (scopes.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(s -> TeacherTeachingScopeUtil.studentAccessible(s, scopes))
                .toList();
    }

    public ClassroomTaskEntity requireTask(String taskId) {
        ClassroomTaskEntity task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        return task;
    }

    public ClassroomTaskEntity requirePublishedTask(String taskId) {
        ClassroomTaskEntity task = requireTask(taskId);
        if (!Boolean.TRUE.equals(task.getPublished())) {
            throw new IllegalArgumentException("任务尚未发布");
        }
        if (!task.getClassId().equals(AuthContext.require().classId())) {
            throw new IllegalArgumentException("无权访问此任务");
        }
        AuthUser user = AuthContext.require();
        if (user.role() == UserRole.STUDENT) {
            SysUserEntity student = sysUserMapper.findById(user.id());
            if (!ClassroomTaskTargeting.matches(task, student)) {
                throw new IllegalArgumentException("无权访问此任务");
            }
        }
        return task;
    }

    public ClassroomSubTaskEntity requireSubTask(String taskId, String subTaskId) {
        ClassroomSubTaskEntity sub = subTaskMapper.findOne(taskId, subTaskId);
        if (sub == null) {
            throw new IllegalArgumentException("子任务不存在");
        }
        return sub;
    }

    public ClassroomSubmissionEntity requireSubmission(String submissionId) {
        ClassroomSubmissionEntity s = submissionMapper.findById(submissionId);
        if (s == null) {
            throw new IllegalArgumentException("提交记录不存在");
        }
        return s;
    }

    public static String blank(String v, String def) {
        return v == null || v.isBlank() ? def : v.trim();
    }

    public static String trimOrNull(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        return v.trim();
    }

    public static String trimRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public static void validateTaskSchedule(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("截至时间不能早于开始时间");
        }
    }
}
