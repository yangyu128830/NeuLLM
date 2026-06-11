package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.dto.classroom.UpsertStudentRequest;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentRosterEntry;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentScopeOptionsResponse;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.auth.UserSessionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.notification.UserNotificationMapper;
import com.neusoft.edu.neullmdev.service.auth.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClassroomStudentService {

    private final ClassroomAccessSupport access;
    private final SysUserMapper sysUserMapper;
    private final UserSessionMapper userSessionMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final ClassroomSubmissionMapper submissionMapper;
    private final PasswordHasher passwordHasher;

    public ClassroomStudentService(ClassroomAccessSupport access,
                                   SysUserMapper sysUserMapper,
                                   UserSessionMapper userSessionMapper,
                                   UserNotificationMapper userNotificationMapper,
                                   ClassroomSubmissionMapper submissionMapper,
                                   PasswordHasher passwordHasher) {
        this.access = access;
        this.sysUserMapper = sysUserMapper;
        this.userSessionMapper = userSessionMapper;
        this.userNotificationMapper = userNotificationMapper;
        this.submissionMapper = submissionMapper;
        this.passwordHasher = passwordHasher;
    }

    public List<StudentResponse> listStudents(String classId) {
        access.requireTeacher();
        return access.listAccessibleStudents(classId).stream()
                .map(ClassroomVoMapper::toStudent)
                .toList();
    }

    public StudentScopeOptionsResponse listStudentScopeOptions() {
        access.requireTeacher();
        String cid = AuthContext.require().classId();
        Set<String> majors = new LinkedHashSet<>();
        Set<String> grades = new LinkedHashSet<>();
        Set<String> classNames = new LinkedHashSet<>();
        List<StudentRosterEntry> roster = new ArrayList<>();
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(cid)) {
            if (s.getMajor() != null && !s.getMajor().isBlank()) {
                majors.add(s.getMajor().trim());
            }
            if (s.getGrade() != null && !s.getGrade().isBlank()) {
                grades.add(s.getGrade().trim());
            }
            if (s.getClassName() != null && !s.getClassName().isBlank()) {
                classNames.add(s.getClassName().trim());
            }
            roster.add(ClassroomVoMapper.toRosterEntry(s));
        }
        return new StudentScopeOptionsResponse(
                new ArrayList<>(majors),
                new ArrayList<>(grades),
                new ArrayList<>(classNames),
                roster);
    }

    public StudentResponse getStudent(Long studentUserId) {
        access.requireTeacher();
        return ClassroomVoMapper.toStudent(access.requireClassStudent(studentUserId));
    }

    @Transactional
    public StudentResponse createStudent(UpsertStudentRequest request) {
        access.requireTeacher();
        AuthUser teacher = AuthContext.require();
        String classId = teacher.classId() == null || teacher.classId().isBlank() ? "CLASS-01" : teacher.classId();
        String displayName = ClassroomAccessSupport.trimRequired(request.getDisplayName(), "请填写姓名");
        String studentNo = ClassroomAccessSupport.trimRequired(request.getStudentNo(), "请填写学号");
        String password = ClassroomAccessSupport.trimRequired(request.getPassword(), "请填写初始密码");
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (sysUserMapper.findStudentByClassAndNo(classId, studentNo) != null) {
            throw new IllegalArgumentException("本班学号已存在");
        }
        String username = resolveUsername(request.getUsername(), studentNo);
        SysUserEntity row = new SysUserEntity();
        row.setUsername(username);
        row.setPasswordHash(passwordHasher.encode(password));
        row.setRole(UserRole.STUDENT.name());
        row.setDisplayName(displayName);
        row.setStudentNo(studentNo);
        row.setClassId(classId);
        applyProfile(row, request);
        sysUserMapper.insert(row);
        return ClassroomVoMapper.toStudent(sysUserMapper.findById(row.getId()));
    }

    @Transactional
    public StudentResponse updateStudent(Long studentUserId, UpsertStudentRequest request) {
        access.requireTeacher();
        SysUserEntity existing = access.requireClassStudent(studentUserId);
        String classId = existing.getClassId();
        String displayName = ClassroomAccessSupport.trimRequired(request.getDisplayName(), "请填写姓名");
        String studentNo = ClassroomAccessSupport.trimRequired(request.getStudentNo(), "请填写学号");
        if (!studentNo.equals(existing.getStudentNo())) {
            SysUserEntity conflict = sysUserMapper.findStudentByClassAndNo(classId, studentNo);
            if (conflict != null && !conflict.getId().equals(studentUserId)) {
                throw new IllegalArgumentException("本班学号已存在");
            }
        }
        String username = resolveUsernameForUpdate(request.getUsername(), studentNo, existing.getUsername());
        if (!username.equals(existing.getUsername())) {
            if (sysUserMapper.findByUsername(username) != null) {
                throw new IllegalArgumentException("登录用户名已存在");
            }
        }
        existing.setUsername(username);
        existing.setDisplayName(displayName);
        existing.setStudentNo(studentNo);
        applyProfile(existing, request);
        String password = request.getPassword() == null ? null : request.getPassword().trim();
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            existing.setPasswordHash(passwordHasher.encode(password));
            sysUserMapper.update(existing);
        } else {
            sysUserMapper.updateWithoutPassword(existing);
        }
        return ClassroomVoMapper.toStudent(sysUserMapper.findById(studentUserId));
    }

    @Transactional
    public void deleteStudent(Long studentUserId) {
        access.requireTeacher();
        access.requireClassStudent(studentUserId);
        submissionMapper.deleteByStudentUserId(studentUserId);
        userNotificationMapper.deleteByUserId(studentUserId);
        userSessionMapper.deleteByUserId(studentUserId);
        sysUserMapper.deleteById(studentUserId);
    }

    private void applyProfile(SysUserEntity row, UpsertStudentRequest request) {
        row.setMajor(ClassroomAccessSupport.trimRequired(request.getMajor(), "请填写专业"));
        row.setGrade(ClassroomAccessSupport.trimRequired(request.getGrade(), "请填写年级"));
        row.setClassName(ClassroomAccessSupport.trimRequired(request.getClassName(), "请填写班级"));
    }

    private String resolveUsername(String requested, String studentNo) {
        String base = requested == null || requested.isBlank() ? "s" + studentNo : requested.trim();
        String candidate = base;
        int suffix = 1;
        while (sysUserMapper.findByUsername(candidate) != null) {
            candidate = base + suffix;
            suffix += 1;
        }
        return candidate;
    }

    private String resolveUsernameForUpdate(String requested, String studentNo, String current) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        return current == null || current.isBlank() ? "s" + studentNo : current;
    }
}
