package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.dto.classroom.UpsertStudentRequest;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.auth.UserSessionMapper;
import com.neusoft.edu.neullmdev.mapper.notification.UserNotificationMapper;
import com.neusoft.edu.neullmdev.dto.auth.TeachingScopeItem;
import com.neusoft.edu.neullmdev.service.auth.PasswordHasher;
import com.neusoft.edu.neullmdev.service.auth.TeacherTeachingScopeUtil;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomTaskMapper;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import com.neusoft.edu.neullmdev.model.notification.NotificationType;
import com.neusoft.edu.neullmdev.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClassroomService {

    private final ClassroomTaskMapper taskMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final ClassroomSubmissionMapper submissionMapper;
    private final SysUserMapper sysUserMapper;
    private final UserSessionMapper userSessionMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final PasswordHasher passwordHasher;
    private final DocumentTextExtractor textExtractor;
    private final ArtifactStorageService artifactStorage;
    private final NotificationService notificationService;

    public ClassroomService(ClassroomTaskMapper taskMapper,
                            ClassroomSubTaskMapper subTaskMapper,
                            ClassroomSubmissionMapper submissionMapper,
                            SysUserMapper sysUserMapper,
                            UserSessionMapper userSessionMapper,
                            UserNotificationMapper userNotificationMapper,
                            PasswordHasher passwordHasher,
                            DocumentTextExtractor textExtractor,
                            ArtifactStorageService artifactStorage,
                            NotificationService notificationService) {
        this.taskMapper = taskMapper;
        this.subTaskMapper = subTaskMapper;
        this.submissionMapper = submissionMapper;
        this.sysUserMapper = sysUserMapper;
        this.userSessionMapper = userSessionMapper;
        this.userNotificationMapper = userNotificationMapper;
        this.passwordHasher = passwordHasher;
        this.textExtractor = textExtractor;
        this.artifactStorage = artifactStorage;
        this.notificationService = notificationService;
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

    public List<Map<String, Object>> listStudents(String classId) {
        requireTeacher();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUserEntity s : listAccessibleStudents(classId)) {
            result.add(studentVo(s));
        }
        return result;
    }

    public Map<String, Object> listStudentScopeOptions() {
        requireTeacher();
        String cid = AuthContext.require().classId();
        LinkedHashMap<String, java.util.Set<String>> buckets = new LinkedHashMap<>();
        buckets.put("majors", new java.util.LinkedHashSet<>());
        buckets.put("grades", new java.util.LinkedHashSet<>());
        buckets.put("classNames", new java.util.LinkedHashSet<>());
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(cid)) {
            if (s.getMajor() != null && !s.getMajor().isBlank()) {
                buckets.get("majors").add(s.getMajor().trim());
            }
            if (s.getGrade() != null && !s.getGrade().isBlank()) {
                buckets.get("grades").add(s.getGrade().trim());
            }
            if (s.getClassName() != null && !s.getClassName().isBlank()) {
                buckets.get("classNames").add(s.getClassName().trim());
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("majors", new ArrayList<>(buckets.get("majors")));
        out.put("grades", new ArrayList<>(buckets.get("grades")));
        out.put("classNames", new ArrayList<>(buckets.get("classNames")));
        List<Map<String, String>> roster = new ArrayList<>();
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(cid)) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("major", s.getMajor());
            row.put("grade", s.getGrade());
            row.put("className", s.getClassName());
            roster.add(row);
        }
        out.put("roster", roster);
        return out;
    }

    public Map<String, Object> getStudent(Long studentUserId) {
        requireTeacher();
        SysUserEntity student = requireClassStudent(studentUserId);
        return studentVo(student);
    }

    @Transactional
    public Map<String, Object> createStudent(UpsertStudentRequest request) {
        requireTeacher();
        AuthUser teacher = AuthContext.require();
        String classId = teacher.classId() == null || teacher.classId().isBlank() ? "CLASS-01" : teacher.classId();
        String displayName = trimRequired(request.getDisplayName(), "请填写姓名");
        String studentNo = trimRequired(request.getStudentNo(), "请填写学号");
        String password = trimRequired(request.getPassword(), "请填写初始密码");
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
        return studentVo(sysUserMapper.findById(row.getId()));
    }

    @Transactional
    public Map<String, Object> updateStudent(Long studentUserId, UpsertStudentRequest request) {
        requireTeacher();
        SysUserEntity existing = requireClassStudent(studentUserId);
        String classId = existing.getClassId();
        String displayName = trimRequired(request.getDisplayName(), "请填写姓名");
        String studentNo = trimRequired(request.getStudentNo(), "请填写学号");
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
        return studentVo(sysUserMapper.findById(studentUserId));
    }

    @Transactional
    public void deleteStudent(Long studentUserId) {
        requireTeacher();
        requireClassStudent(studentUserId);
        submissionMapper.deleteByStudentUserId(studentUserId);
        userNotificationMapper.deleteByUserId(studentUserId);
        userSessionMapper.deleteByUserId(studentUserId);
        sysUserMapper.deleteById(studentUserId);
    }

    private SysUserEntity requireClassStudent(Long studentUserId) {
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

    private List<TeachingScopeItem> currentTeacherScopes() {
        SysUserEntity teacher = sysUserMapper.findById(AuthContext.require().id());
        if (teacher == null) {
            return List.of();
        }
        return TeacherTeachingScopeUtil.parseScopes(teacher.getTeachingScopes());
    }

    private List<SysUserEntity> listAccessibleStudents(String classId) {
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

    private Map<String, Object> studentVo(SysUserEntity s) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("studentId", s.getStudentNo());
        row.put("studentUserId", s.getId());
        row.put("name", s.getDisplayName());
        row.put("username", s.getUsername());
        row.put("classId", s.getClassId());
        row.put("major", s.getMajor());
        row.put("grade", s.getGrade());
        row.put("className", s.getClassName());
        return row;
    }

    private void applyProfile(SysUserEntity row, UpsertStudentRequest request) {
        row.setMajor(trimRequired(request.getMajor(), "请填写专业"));
        row.setGrade(trimRequired(request.getGrade(), "请填写年级"));
        row.setClassName(trimRequired(request.getClassName(), "请填写班级"));
    }

    private static String trimRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
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

    @Transactional
    public Map<String, Object> createTask(CreateTaskRequest request) {
        requireTeacher();
        AuthUser teacher = AuthContext.require();
        String taskId = "TASK-" + String.format("%03d", taskMapper.maxTaskSeq() + 1);
        ClassroomTaskEntity task = new ClassroomTaskEntity();
        task.setTaskId(taskId);
        task.setClassId(teacher.classId());
        task.setTeacherId(teacher.id());
        task.setTitle(blank(request.getTitle(), "课堂任务"));
        task.setDescription(blank(request.getDescription(), ""));
        task.setSubject(trimOrNull(request.getSubject()));
        task.setTargetMajor(trimOrNull(request.getTargetMajor()));
        task.setTargetGrade(trimOrNull(request.getTargetGrade()));
        task.setTargetClassName(trimOrNull(request.getTargetClassName()));
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        validateTaskSchedule(task.getStartTime(), task.getEndTime());
        task.setPublished(Boolean.TRUE.equals(request.getPublish()));
        taskMapper.insert(task);

        List<CreateTaskRequest.SubTaskItem> items = request.getSubTasks();
        if (items == null || items.isEmpty()) {
            items = defaultSubTasks();
        }
        insertSubTasks(taskId, items);
        return taskDetail(taskId);
    }

    @Transactional
    public Map<String, Object> updateTask(String taskId, CreateTaskRequest request) {
        requireTeacher();
        AuthUser teacher = AuthContext.require();
        ClassroomTaskEntity task = requireTask(taskId);
        if (!task.getTeacherId().equals(teacher.id())) {
            throw new IllegalArgumentException("无权修改此任务");
        }
        validateTaskSchedule(request.getStartTime(), request.getEndTime());
        ClassroomTaskEntity meta = new ClassroomTaskEntity();
        meta.setTaskId(taskId);
        meta.setTitle(blank(request.getTitle(), "课堂任务"));
        meta.setDescription(blank(request.getDescription(), ""));
        meta.setSubject(trimOrNull(request.getSubject()));
        meta.setTargetMajor(trimOrNull(request.getTargetMajor()));
        meta.setTargetGrade(trimOrNull(request.getTargetGrade()));
        meta.setTargetClassName(trimOrNull(request.getTargetClassName()));
        meta.setStartTime(request.getStartTime());
        meta.setEndTime(request.getEndTime());
        taskMapper.updateMeta(meta);

        List<CreateTaskRequest.SubTaskItem> items = request.getSubTasks();
        if (items == null || items.isEmpty()) {
            items = defaultSubTasks();
        }
        if (!Boolean.TRUE.equals(task.getPublished())) {
            subTaskMapper.deleteByTask(taskId);
            insertSubTasks(taskId, items);
        } else {
            syncPublishedSubTasks(taskId, items);
        }

        if (Boolean.TRUE.equals(request.getPublish()) && !Boolean.TRUE.equals(task.getPublished())) {
            return publishTask(taskId);
        }
        return taskDetail(taskId);
    }

    @Transactional
    public void deleteTask(String taskId) {
        requireTeacher();
        AuthUser teacher = AuthContext.require();
        ClassroomTaskEntity task = requireTask(taskId);
        if (!task.getTeacherId().equals(teacher.id())) {
            throw new IllegalArgumentException("无权删除此任务");
        }
        for (ClassroomSubmissionEntity submission : submissionMapper.listByTask(taskId)) {
            artifactStorage.deleteSubmissionArtifacts(submission.getSubmissionId());
        }
        submissionMapper.deleteByTask(taskId);
        subTaskMapper.deleteByTask(taskId);
        taskMapper.delete(taskId);
    }

    @Transactional
    public Map<String, Object> publishTask(String taskId) {
        requireTeacher();
        ClassroomTaskEntity task = requireTask(taskId);
        if (!task.getTeacherId().equals(AuthContext.require().id())) {
            throw new IllegalArgumentException("无权发布此任务");
        }
        taskMapper.publish(taskId);
        notificationService.notifyTargetStudents(
                task.getClassId(),
                task.getTargetMajor(),
                task.getTargetGrade(),
                task.getTargetClassName(),
                NotificationType.TASK_PUBLISHED,
                "新作业发布：" + task.getTitle(),
                blank(task.getDescription(), "").isEmpty()
                        ? "教师发布了新的课堂作业，请前往「我的作业」查看并提交。"
                        : task.getDescription(),
                "/assignments",
                taskId);
        return taskDetail(taskId);
    }

    public List<Map<String, Object>> listTasksForTeacher() {
        requireTeacher();
        return listTasks(taskMapper.listByClass(AuthContext.require().classId()));
    }

    public List<Map<String, Object>> listMyAssignments() {
        requireStudent();
        AuthUser user = AuthContext.require();
        Long studentId = user.id();
        SysUserEntity student = sysUserMapper.findById(studentId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ClassroomTaskEntity t : taskMapper.listPublishedByClass(user.classId())) {
            if (!ClassroomTaskTargeting.matches(t, student)) {
                continue;
            }
            list.add(taskDetailForStudent(t.getTaskId(), studentId));
        }
        return list;
    }

    @Transactional
    public Map<String, Object> submitFile(String taskId, String subTaskId, MultipartFile file) throws IOException {
        requireStudent();
        AuthUser student = AuthContext.require();
        ClassroomTaskEntity task = requirePublishedTask(taskId);
        ClassroomSubTaskEntity subTask = requireSubTask(taskId, subTaskId);
        if (!canSubmit(taskId, subTask, student.id())) {
            throw new IllegalArgumentException("前置子任务尚未提交，或当前状态不可提交");
        }
        byte[] bytes = file.getBytes();
        String content = textExtractor.extract(file.getOriginalFilename(), bytes);
        return saveSubmission(task, subTask, student, file.getOriginalFilename(), content, bytes);
    }

    @Transactional
    public Map<String, Object> submitText(String taskId, String subTaskId, String fileName, String content) {
        requireStudent();
        AuthUser student = AuthContext.require();
        ClassroomTaskEntity task = requirePublishedTask(taskId);
        ClassroomSubTaskEntity subTask = requireSubTask(taskId, subTaskId);
        if (!canSubmit(taskId, subTask, student.id())) {
            throw new IllegalArgumentException("前置子任务尚未提交，或当前状态不可提交");
        }
        try {
            return saveSubmission(task, subTask, student, fileName, content, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("保存提交失败：" + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> gradeSubmission(String submissionId, double score, String comment) {
        requireTeacher();
        ClassroomSubmissionEntity sub = requireSubmission(submissionId);
        submissionMapper.grade(submissionId, BigDecimal.valueOf(score), comment, AuthContext.require().id());
        ClassroomSubmissionEntity graded = submissionMapper.findById(submissionId);
        notificationService.notifyUser(
                graded.getStudentUserId(),
                NotificationType.GRADE_RESULT,
                "作业已批改",
                "您的提交已评分 " + score + " 分。" + (comment != null && !comment.isBlank() ? " 评语：" + comment : ""),
                "/assignments",
                graded.getTaskId());
        return submissionVo(graded);
    }

    @Transactional
    public Map<String, Object> rejectSubmission(String submissionId, String comment) {
        requireTeacher();
        ClassroomSubmissionEntity sub = requireSubmission(submissionId);
        submissionMapper.reject(submissionId, comment, AuthContext.require().id());
        notificationService.notifyUser(
                sub.getStudentUserId(),
                NotificationType.TASK_REMIND,
                "作业需重新提交",
                (comment != null && !comment.isBlank() ? comment : "请根据教师意见修改后重新提交。"),
                "/assignments",
                sub.getTaskId());
        return submissionVo(submissionMapper.findById(submissionId));
    }

    public Map<String, Object> taskDetail(String taskId) {
        ClassroomTaskEntity task = requireTask(taskId);
        return buildTaskDetailMap(taskId, task);
    }

    public Map<String, Object> getTaskForTeacher(String taskId) {
        requireTeacher();
        ClassroomTaskEntity task = requireTask(taskId);
        if (!task.getTeacherId().equals(AuthContext.require().id())) {
            throw new IllegalArgumentException("无权查看此任务");
        }
        return buildTaskDetailMap(taskId, task);
    }

    public List<Map<String, Object>> listSubmissions(String taskId) {
        requireTeacher();
        requireTask(taskId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ClassroomSubmissionEntity s : submissionMapper.listByTask(taskId)) {
            Map<String, Object> vo = submissionVo(s);
            SysUserEntity student = sysUserMapper.findById(s.getStudentUserId());
            if (student != null) {
                vo.put("studentName", student.getDisplayName());
                vo.put("studentNo", student.getStudentNo());
            }
            list.add(vo);
        }
        return list;
    }

    public boolean canSubmit(String taskId, ClassroomSubTaskEntity subTask, Long studentUserId) {
        ClassroomSubmissionEntity existing = submissionMapper.findCell(taskId, subTask.getSubTaskId(), studentUserId);
        if (existing != null && SubmissionStatus.REJECTED.name().equals(existing.getStatus())) {
            return true;
        }
        if (existing != null) {
            return false;
        }
        if (subTask.getOrderNo() == 1) {
            return true;
        }
        Optional<ClassroomSubTaskEntity> prev = subTaskMapper.listByTask(taskId).stream()
                .filter(s -> s.getOrderNo() == subTask.getOrderNo() - 1)
                .findFirst();
        return prev.map(p -> submissionMapper.findCell(taskId, p.getSubTaskId(), studentUserId) != null)
                .orElse(true);
    }

    private Map<String, Object> saveSubmission(ClassroomTaskEntity task,
                                               ClassroomSubTaskEntity subTask,
                                               AuthUser student,
                                               String fileName,
                                               String content,
                                               byte[] bytes) throws IOException {
        ClassroomSubmissionEntity existing = submissionMapper.findCell(
                task.getTaskId(), subTask.getSubTaskId(), student.id());
        String submissionId;
        if (existing != null && SubmissionStatus.REJECTED.name().equals(existing.getStatus())) {
            submissionId = existing.getSubmissionId();
            if (bytes != null) {
                artifactStorage.deleteSubmissionArtifacts(submissionId);
                String path = artifactStorage.store(submissionId, fileName, bytes);
                existing.setArtifactPath(path);
            }
            existing.setFileName(blank(fileName, subTask.getTitle() + ".txt"));
            existing.setContent(blank(content, ""));
            submissionMapper.resubmit(existing);
        } else if (existing != null) {
            throw new IllegalArgumentException("该子任务已提交，若需重交请等待教师打回");
        } else {
            submissionId = "SUBMIT-" + String.format("%03d", submissionMapper.maxSubmissionSeq() + 1);
            ClassroomSubmissionEntity row = new ClassroomSubmissionEntity();
            row.setSubmissionId(submissionId);
            row.setTaskId(task.getTaskId());
            row.setSubTaskId(subTask.getSubTaskId());
            row.setStudentUserId(student.id());
            row.setFileName(blank(fileName, subTask.getTitle() + ".txt"));
            row.setContent(blank(content, ""));
            row.setStatus(SubmissionStatus.SUBMITTED.name());
            row.setSubmittedAt(LocalDateTime.now());
            if (bytes != null) {
                row.setArtifactPath(artifactStorage.store(submissionId, fileName, bytes));
            }
            submissionMapper.insert(row);
        }
        return submissionVo(submissionMapper.findById(submissionId));
    }

    private List<Map<String, Object>> listTasks(List<ClassroomTaskEntity> tasks) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ClassroomTaskEntity t : tasks) {
            list.add(taskDetail(t.getTaskId()));
        }
        return list;
    }

    private ClassroomTaskEntity requireTask(String taskId) {
        ClassroomTaskEntity task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        return task;
    }

    private ClassroomTaskEntity requirePublishedTask(String taskId) {
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

    private ClassroomSubTaskEntity requireSubTask(String taskId, String subTaskId) {
        ClassroomSubTaskEntity sub = subTaskMapper.findOne(taskId, subTaskId);
        if (sub == null) {
            throw new IllegalArgumentException("子任务不存在");
        }
        return sub;
    }

    private ClassroomSubmissionEntity requireSubmission(String submissionId) {
        ClassroomSubmissionEntity s = submissionMapper.findById(submissionId);
        if (s == null) {
            throw new IllegalArgumentException("提交记录不存在");
        }
        return s;
    }

    private Map<String, Object> taskDetailForStudent(String taskId, Long studentUserId) {
        Map<String, Object> map = taskDetail(taskId);
        List<ClassroomSubTaskEntity> subs = subTaskMapper.listByTask(taskId);
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (ClassroomSubTaskEntity sub : subs) {
            enriched.add(subTaskVoForStudent(taskId, sub, studentUserId));
        }
        map.put("subTasks", enriched);
        return map;
    }

    private void insertSubTasks(String taskId, List<CreateTaskRequest.SubTaskItem> items) {
        int order = 1;
        for (CreateTaskRequest.SubTaskItem item : items) {
            ClassroomSubTaskEntity sub = new ClassroomSubTaskEntity();
            sub.setTaskId(taskId);
            sub.setSubTaskId("SUB-" + String.format("%03d", order));
            sub.setOrderNo(order);
            sub.setTitle(blank(item.getTitle(), "子任务 " + order));
            sub.setDescription(blank(item.getDescription(), "请提交成果物。"));
            subTaskMapper.insert(sub);
            order++;
        }
    }

    private void syncPublishedSubTasks(String taskId, List<CreateTaskRequest.SubTaskItem> items) {
        List<ClassroomSubTaskEntity> existing = subTaskMapper.listByTask(taskId);
        if (items.size() < existing.size()) {
            for (int i = items.size(); i < existing.size(); i++) {
                ClassroomSubTaskEntity removed = existing.get(i);
                if (submissionMapper.countBySubTask(taskId, removed.getSubTaskId()) > 0) {
                    throw new IllegalArgumentException("无法删除已有学生提交的子任务");
                }
            }
            for (int i = items.size(); i < existing.size(); i++) {
                subTaskMapper.deleteOne(taskId, existing.get(i).getSubTaskId());
            }
            existing = subTaskMapper.listByTask(taskId);
        }
        int nextSubSeq = existing.stream()
                .mapToInt(s -> Integer.parseInt(s.getSubTaskId().substring(4)))
                .max()
                .orElse(0);
        for (int i = 0; i < items.size(); i++) {
            CreateTaskRequest.SubTaskItem item = items.get(i);
            if (i < existing.size()) {
                ClassroomSubTaskEntity sub = existing.get(i);
                subTaskMapper.updateContent(taskId, sub.getSubTaskId(), i + 1,
                        blank(item.getTitle(), "子任务 " + (i + 1)),
                        blank(item.getDescription(), "请提交成果物。"));
            } else {
                nextSubSeq++;
                ClassroomSubTaskEntity sub = new ClassroomSubTaskEntity();
                sub.setTaskId(taskId);
                sub.setSubTaskId("SUB-" + String.format("%03d", nextSubSeq));
                sub.setOrderNo(i + 1);
                sub.setTitle(blank(item.getTitle(), "子任务 " + (i + 1)));
                sub.setDescription(blank(item.getDescription(), "请提交成果物。"));
                subTaskMapper.insert(sub);
            }
        }
    }

    private Map<String, Object> buildTaskDetailMap(String taskId, ClassroomTaskEntity task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskId", task.getTaskId());
        map.put("classId", task.getClassId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("subject", task.getSubject());
        map.put("targetMajor", task.getTargetMajor());
        map.put("targetGrade", task.getTargetGrade());
        map.put("targetClassName", task.getTargetClassName());
        map.put("startTime", task.getStartTime());
        map.put("endTime", task.getEndTime());
        map.put("published", Boolean.TRUE.equals(task.getPublished()));
        map.put("subTasks", subTaskMapper.listByTask(taskId).stream().map(this::subTaskVo).toList());
        return map;
    }

    private Map<String, Object> subTaskVoForStudent(String taskId, ClassroomSubTaskEntity sub, Long studentUserId) {
        Map<String, Object> m = subTaskVo(sub);
        ClassroomSubmissionEntity subm = submissionMapper.findCell(taskId, sub.getSubTaskId(), studentUserId);
        if (subm != null) {
            m.put("submissionId", subm.getSubmissionId());
            m.put("status", subm.getStatus());
            m.put("fileName", subm.getFileName());
            m.put("submittedAt", subm.getSubmittedAt());
            m.put("score", subm.getScore());
            m.put("teacherComment", subm.getTeacherComment());
            m.put("canSubmit", SubmissionStatus.REJECTED.name().equals(subm.getStatus()));
            m.put("statusLabel", studentStatusLabel(subm));
        } else {
            boolean allowed = canSubmit(taskId, sub, studentUserId);
            m.put("canSubmit", allowed);
            m.put("status", allowed ? "AVAILABLE" : "LOCKED");
            m.put("statusLabel", allowed ? "待提交" : "未解锁");
        }
        return m;
    }

    private String studentStatusLabel(ClassroomSubmissionEntity subm) {
        if (SubmissionStatus.GRADED.name().equals(subm.getStatus())) {
            return subm.getScore() != null ? "已批改 · " + subm.getScore() + " 分" : "已批改";
        }
        if (SubmissionStatus.REJECTED.name().equals(subm.getStatus())) {
            return "需重交";
        }
        return "已提交";
    }

    private Map<String, Object> subTaskVo(ClassroomSubTaskEntity s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("subTaskId", s.getSubTaskId());
        m.put("orderNo", s.getOrderNo());
        m.put("title", s.getTitle());
        m.put("description", s.getDescription());
        return m;
    }

    private Map<String, Object> submissionVo(ClassroomSubmissionEntity s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("submissionId", s.getSubmissionId());
        m.put("taskId", s.getTaskId());
        m.put("subTaskId", s.getSubTaskId());
        m.put("studentUserId", s.getStudentUserId());
        m.put("fileName", s.getFileName());
        m.put("content", s.getContent());
        m.put("status", s.getStatus());
        m.put("score", s.getScore());
        m.put("teacherComment", s.getTeacherComment());
        m.put("submittedAt", s.getSubmittedAt());
        m.put("gradedAt", s.getGradedAt());
        return m;
    }

    private List<CreateTaskRequest.SubTaskItem> defaultSubTasks() {
        List<CreateTaskRequest.SubTaskItem> list = new ArrayList<>();
        String[] titles = {"复杂任务需求分析表", "工具清单与工具契约", "Plan 示例与异常处理方案"};
        for (String t : titles) {
            CreateTaskRequest.SubTaskItem item = new CreateTaskRequest.SubTaskItem();
            item.setTitle(t);
            item.setDescription("请提交「" + t + "」的成果物。");
            list.add(item);
        }
        return list;
    }

    private String blank(String v, String def) {
        return v == null || v.isBlank() ? def : v.trim();
    }

    private String trimOrNull(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        return v.trim();
    }

    private void validateTaskSchedule(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("截至时间不能早于开始时间");
        }
    }

}
