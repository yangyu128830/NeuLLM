package com.neusoft.edu.neullmdev.service.auth;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.dto.auth.AuthResponse;
import com.neusoft.edu.neullmdev.dto.auth.LoginRequest;
import com.neusoft.edu.neullmdev.dto.auth.RegisterRequest;
import com.neusoft.edu.neullmdev.dto.auth.UpdateStudentProfileRequest;
import com.neusoft.edu.neullmdev.dto.auth.UpdateTeacherProfileRequest;
import com.neusoft.edu.neullmdev.dto.auth.TeachingScopeItem;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.auth.UserSessionEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.auth.UserSessionMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserSessionMapper userSessionMapper;
    private final PasswordHasher passwordHasher;
    private final int sessionHours;

    public AuthService(SysUserMapper sysUserMapper,
                       UserSessionMapper userSessionMapper,
                       PasswordHasher passwordHasher,
                       @Value("${app.auth.session-hours:72}") int sessionHours) {
        this.sysUserMapper = sysUserMapper;
        this.userSessionMapper = userSessionMapper;
        this.passwordHasher = passwordHasher;
        this.sessionHours = sessionHours;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (sysUserMapper.findByUsername(request.getUsername().trim()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserRole role = UserRole.from(request.getRole());
        if (role == UserRole.STUDENT && (request.getStudentNo() == null || request.getStudentNo().isBlank())) {
            throw new IllegalArgumentException("学生注册须填写学号");
        }
        SysUserEntity row = new SysUserEntity();
        row.setUsername(request.getUsername().trim());
        row.setPasswordHash(passwordHasher.encode(request.getPassword()));
        row.setRole(role.name());
        row.setDisplayName(request.getDisplayName().trim());
        row.setStudentNo(role == UserRole.STUDENT ? request.getStudentNo().trim() : null);
        row.setClassId(request.getClassId() == null || request.getClassId().isBlank()
                ? "CLASS-01" : request.getClassId().trim());
        sysUserMapper.insert(row);
        return createSession(AuthUser.from(sysUserMapper.findById(row.getId())));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        SysUserEntity user = sysUserMapper.findByUsername(request.getUsername().trim());
        if (user == null || !passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            UserRole expected = UserRole.from(request.getRole());
            UserRole actual = UserRole.from(user.getRole());
            if (expected != actual) {
                throw new IllegalArgumentException(
                        expected == UserRole.TEACHER ? "该账号不是教师账号" : "该账号不是学生账号");
            }
        }
        userSessionMapper.deleteByUserId(user.getId());
        return createSession(AuthUser.from(user));
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            userSessionMapper.deleteByToken(token.trim());
        }
    }

    public AuthUser resolveUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        UserSessionEntity session = userSessionMapper.findByToken(token.trim());
        if (session == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        SysUserEntity user = sysUserMapper.findById(session.getUserId());
        return user == null ? null : AuthUser.from(user);
    }

    public Map<String, Object> getTeacherProfile() {
        SysUserEntity user = requireTeacherUser();
        return teacherProfileVo(user);
    }

    @Transactional
    public Map<String, Object> updateTeacherProfile(UpdateTeacherProfileRequest request) {
        SysUserEntity user = requireTeacherUser();
        String displayName = trimRequired(request.getDisplayName(), "请填写姓名");
        String username = trimRequired(request.getUsername(), "请填写用户名");
        if (!username.equals(user.getUsername())) {
            SysUserEntity conflict = sysUserMapper.findByUsername(username);
            if (conflict != null && !conflict.getId().equals(user.getId())) {
                throw new IllegalArgumentException("用户名已被占用");
            }
        }
        user.setDisplayName(displayName);
        user.setUsername(username);
        user.setStudentNo(trimOrNull(request.getEmployeeNo()));
        user.setMajor(trimOrNull(request.getDepartment()));
        user.setGrade(trimOrNull(request.getTitle()));
        user.setEmail(trimOrNull(request.getEmail()));
        user.setPhone(trimOrNull(request.getPhone()));
        List<String> taughtSubjects = request.getTaughtSubjects() == null
                ? List.of() : request.getTaughtSubjects();
        if (taughtSubjects.stream().noneMatch(s -> s != null && !s.isBlank())) {
            throw new IllegalArgumentException("请至少选择一门所教课程");
        }
        List<TeachingScopeItem> teachingScopes = TeacherTeachingScopeUtil.parseScopesFromRequest(
                request.getTeachingScopes());
        if (teachingScopes.isEmpty()) {
            throw new IllegalArgumentException("请至少添加一个教学板块");
        }
        user.setTaughtSubjects(TeacherTeachingScopeUtil.serializeSubjects(taughtSubjects));
        user.setTeachingScopes(TeacherTeachingScopeUtil.serializeScopes(teachingScopes));
        String password = request.getPassword();
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            user.setPasswordHash(passwordHasher.encode(password));
            sysUserMapper.update(user);
        } else {
            sysUserMapper.updateWithoutPassword(user);
        }
        return teacherProfileVo(sysUserMapper.findById(user.getId()));
    }

    public Map<String, Object> getStudentProfile() {
        SysUserEntity user = requireStudentUser();
        return studentProfileVo(user);
    }

    @Transactional
    public Map<String, Object> updateStudentProfile(UpdateStudentProfileRequest request) {
        SysUserEntity user = requireStudentUser();
        String displayName = trimRequired(request.getDisplayName(), "请填写姓名");
        String username = trimRequired(request.getUsername(), "请填写登录用户名");
        String studentNo = trimRequired(request.getStudentNo(), "请填写学号");
        String major = trimRequired(request.getMajor(), "请填写专业");
        String grade = trimRequired(request.getGrade(), "请填写年级");
        String className = trimRequired(request.getClassName(), "请填写班级");

        if (!username.equals(user.getUsername())) {
            SysUserEntity conflict = sysUserMapper.findByUsername(username);
            if (conflict != null && !conflict.getId().equals(user.getId())) {
                throw new IllegalArgumentException("登录用户名已被占用");
            }
        }
        if (!studentNo.equals(user.getStudentNo())) {
            SysUserEntity conflict = sysUserMapper.findByStudentNo(studentNo);
            if (conflict != null && !conflict.getId().equals(user.getId())) {
                throw new IllegalArgumentException("学号已被其他账号使用");
            }
        }

        user.setDisplayName(displayName);
        user.setUsername(username);
        user.setStudentNo(studentNo);
        user.setMajor(major);
        user.setGrade(grade);
        user.setClassName(className);
        user.setEmail(trimOrNull(request.getEmail()));
        user.setPhone(trimOrNull(request.getPhone()));

        String password = request.getPassword();
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            user.setPasswordHash(passwordHasher.encode(password));
            sysUserMapper.update(user);
        } else {
            sysUserMapper.updateWithoutPassword(user);
        }
        return studentProfileVo(sysUserMapper.findById(user.getId()));
    }

    private SysUserEntity requireTeacherUser() {
        AuthUser auth = AuthContext.require();
        if (auth.role() != UserRole.TEACHER) {
            throw new IllegalArgumentException("仅教师可访问");
        }
        SysUserEntity user = sysUserMapper.findById(auth.id());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private SysUserEntity requireStudentUser() {
        AuthUser auth = AuthContext.require();
        if (auth.role() != UserRole.STUDENT) {
            throw new IllegalArgumentException("仅学生可访问");
        }
        SysUserEntity user = sysUserMapper.findById(auth.id());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private Map<String, Object> studentProfileVo(SysUserEntity user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("userId", user.getId());
        row.put("username", user.getUsername());
        row.put("displayName", user.getDisplayName());
        row.put("studentNo", user.getStudentNo());
        row.put("major", user.getMajor());
        row.put("grade", user.getGrade());
        row.put("className", user.getClassName());
        row.put("classId", user.getClassId());
        row.put("email", user.getEmail());
        row.put("phone", user.getPhone());
        row.put("profileComplete", isStudentProfileComplete(user));
        row.put("createdAt", user.getCreatedAt());
        row.put("updatedAt", user.getUpdatedAt());
        return row;
    }

    private static boolean isStudentProfileComplete(SysUserEntity user) {
        return isFilled(user.getStudentNo())
                && isFilled(user.getMajor())
                && isFilled(user.getGrade())
                && isFilled(user.getClassName())
                && isFilled(user.getDisplayName());
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }

    private Map<String, Object> teacherProfileVo(SysUserEntity user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("userId", user.getId());
        row.put("username", user.getUsername());
        row.put("displayName", user.getDisplayName());
        row.put("employeeNo", user.getStudentNo());
        row.put("department", user.getMajor());
        row.put("title", user.getGrade());
        row.put("email", user.getEmail());
        row.put("phone", user.getPhone());
        row.put("classId", user.getClassId());
        row.put("taughtSubjects", TeacherTeachingScopeUtil.subjectsVo(user.getTaughtSubjects()));
        row.put("teachingScopes", TeacherTeachingScopeUtil.scopesVo(user.getTeachingScopes()));
        row.put("accessibleStudentCount", countAccessibleStudents(user));
        row.put("createdAt", user.getCreatedAt());
        row.put("updatedAt", user.getUpdatedAt());
        return row;
    }

    private int countAccessibleStudents(SysUserEntity teacher) {
        List<TeachingScopeItem> scopes = TeacherTeachingScopeUtil.parseScopes(teacher.getTeachingScopes());
        if (scopes.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(teacher.getClassId())) {
            if (TeacherTeachingScopeUtil.studentAccessible(s, scopes)) {
                count++;
            }
        }
        return count;
    }

    private static String trimRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private AuthResponse createSession(AuthUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        UserSessionEntity session = new UserSessionEntity();
        session.setToken(token);
        session.setUserId(user.id());
        session.setExpiresAt(LocalDateTime.now().plusHours(sessionHours));
        userSessionMapper.insert(session);
        return AuthResponse.of(token, user);
    }
}
