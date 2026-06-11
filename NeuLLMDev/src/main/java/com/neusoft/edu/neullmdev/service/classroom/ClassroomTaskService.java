package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentAssignmentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentSubTaskResponse;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomTaskMapper;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import com.neusoft.edu.neullmdev.model.notification.NotificationType;
import com.neusoft.edu.neullmdev.service.notification.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassroomTaskService {

    private final ClassroomAccessSupport access;
    private final ClassroomTaskMapper taskMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final ClassroomSubmissionMapper submissionMapper;
    private final ArtifactStorageService artifactStorage;
    private final NotificationService notificationService;
    private final ClassroomSubmissionService submissionService;

    public ClassroomTaskService(ClassroomAccessSupport access,
                                ClassroomTaskMapper taskMapper,
                                ClassroomSubTaskMapper subTaskMapper,
                                ClassroomSubmissionMapper submissionMapper,
                                ArtifactStorageService artifactStorage,
                                NotificationService notificationService,
                                @Lazy ClassroomSubmissionService submissionService) {
        this.access = access;
        this.taskMapper = taskMapper;
        this.subTaskMapper = subTaskMapper;
        this.submissionMapper = submissionMapper;
        this.artifactStorage = artifactStorage;
        this.notificationService = notificationService;
        this.submissionService = submissionService;
    }

    @Transactional
    public ClassroomTaskResponse createTask(CreateTaskRequest request) {
        access.requireTeacher();
        AuthUser teacher = AuthContext.require();
        String taskId = "TASK-" + String.format("%03d", taskMapper.maxTaskSeq() + 1);
        ClassroomTaskEntity task = new ClassroomTaskEntity();
        task.setTaskId(taskId);
        task.setClassId(teacher.classId());
        task.setTeacherId(teacher.id());
        task.setTitle(ClassroomAccessSupport.blank(request.getTitle(), "课堂任务"));
        task.setDescription(ClassroomAccessSupport.blank(request.getDescription(), ""));
        task.setSubject(ClassroomAccessSupport.trimOrNull(request.getSubject()));
        task.setTargetMajor(ClassroomAccessSupport.trimOrNull(request.getTargetMajor()));
        task.setTargetGrade(ClassroomAccessSupport.trimOrNull(request.getTargetGrade()));
        task.setTargetClassName(ClassroomAccessSupport.trimOrNull(request.getTargetClassName()));
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        ClassroomAccessSupport.validateTaskSchedule(task.getStartTime(), task.getEndTime());
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
    public ClassroomTaskResponse updateTask(String taskId, CreateTaskRequest request) {
        access.requireTeacher();
        AuthUser teacher = AuthContext.require();
        ClassroomTaskEntity task = access.requireTask(taskId);
        if (!task.getTeacherId().equals(teacher.id())) {
            throw new IllegalArgumentException("无权修改此任务");
        }
        ClassroomAccessSupport.validateTaskSchedule(request.getStartTime(), request.getEndTime());
        ClassroomTaskEntity meta = new ClassroomTaskEntity();
        meta.setTaskId(taskId);
        meta.setTitle(ClassroomAccessSupport.blank(request.getTitle(), "课堂任务"));
        meta.setDescription(ClassroomAccessSupport.blank(request.getDescription(), ""));
        meta.setSubject(ClassroomAccessSupport.trimOrNull(request.getSubject()));
        meta.setTargetMajor(ClassroomAccessSupport.trimOrNull(request.getTargetMajor()));
        meta.setTargetGrade(ClassroomAccessSupport.trimOrNull(request.getTargetGrade()));
        meta.setTargetClassName(ClassroomAccessSupport.trimOrNull(request.getTargetClassName()));
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
        access.requireTeacher();
        AuthUser teacher = AuthContext.require();
        ClassroomTaskEntity task = access.requireTask(taskId);
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
    public ClassroomTaskResponse publishTask(String taskId) {
        access.requireTeacher();
        ClassroomTaskEntity task = access.requireTask(taskId);
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
                ClassroomAccessSupport.blank(task.getDescription(), "").isEmpty()
                        ? "教师发布了新的课堂作业，请前往「我的作业」查看并提交。"
                        : task.getDescription(),
                "/assignments",
                taskId);
        return taskDetail(taskId);
    }

    public List<ClassroomTaskResponse> listTasksForTeacher() {
        access.requireTeacher();
        return taskMapper.listByClass(AuthContext.require().classId()).stream()
                .map(t -> taskDetail(t.getTaskId()))
                .toList();
    }

    public List<StudentAssignmentResponse> listMyAssignments() {
        access.requireStudent();
        AuthUser user = AuthContext.require();
        Long studentId = user.id();
        SysUserEntity student = access.sysUserMapper().findById(studentId);
        List<StudentAssignmentResponse> list = new ArrayList<>();
        for (ClassroomTaskEntity t : taskMapper.listPublishedByClass(user.classId())) {
            if (!ClassroomTaskTargeting.matches(t, student)) {
                continue;
            }
            list.add(taskDetailForStudent(t.getTaskId(), studentId));
        }
        return list;
    }

    public ClassroomTaskResponse taskDetail(String taskId) {
        ClassroomTaskEntity task = access.requireTask(taskId);
        return buildTaskResponse(taskId, task);
    }

    public ClassroomTaskResponse getTaskForTeacher(String taskId) {
        access.requireTeacher();
        ClassroomTaskEntity task = access.requireTask(taskId);
        if (!task.getTeacherId().equals(AuthContext.require().id())) {
            throw new IllegalArgumentException("无权查看此任务");
        }
        return buildTaskResponse(taskId, task);
    }

    private StudentAssignmentResponse taskDetailForStudent(String taskId, Long studentUserId) {
        ClassroomTaskEntity task = access.requireTask(taskId);
        List<StudentSubTaskResponse> enriched = subTaskMapper.listByTask(taskId).stream()
                .map(sub -> subTaskForStudent(taskId, sub, studentUserId))
                .toList();
        return new StudentAssignmentResponse(
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
                enriched);
    }

    private StudentSubTaskResponse subTaskForStudent(String taskId, ClassroomSubTaskEntity sub, Long studentUserId) {
        ClassroomSubmissionEntity subm = submissionMapper.findCell(taskId, sub.getSubTaskId(), studentUserId);
        if (subm != null) {
            return new StudentSubTaskResponse(
                    sub.getSubTaskId(),
                    sub.getOrderNo(),
                    sub.getTitle(),
                    sub.getDescription(),
                    subm.getSubmissionId(),
                    subm.getStatus(),
                    studentStatusLabel(subm),
                    subm.getFileName(),
                    subm.getSubmittedAt(),
                    subm.getScore(),
                    subm.getTeacherComment(),
                    SubmissionStatus.REJECTED.name().equals(subm.getStatus()));
        }
        boolean allowed = submissionService.canSubmit(taskId, sub, studentUserId);
        return new StudentSubTaskResponse(
                sub.getSubTaskId(),
                sub.getOrderNo(),
                sub.getTitle(),
                sub.getDescription(),
                null,
                allowed ? "AVAILABLE" : "LOCKED",
                allowed ? "待提交" : "未解锁",
                null,
                null,
                null,
                null,
                allowed);
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

    private ClassroomTaskResponse buildTaskResponse(String taskId, ClassroomTaskEntity task) {
        return ClassroomVoMapper.toTask(task, subTaskMapper.listByTask(taskId));
    }

    private void insertSubTasks(String taskId, List<CreateTaskRequest.SubTaskItem> items) {
        int order = 1;
        for (CreateTaskRequest.SubTaskItem item : items) {
            ClassroomSubTaskEntity sub = new ClassroomSubTaskEntity();
            sub.setTaskId(taskId);
            sub.setSubTaskId("SUB-" + String.format("%03d", order));
            sub.setOrderNo(order);
            sub.setTitle(ClassroomAccessSupport.blank(item.getTitle(), "子任务 " + order));
            sub.setDescription(ClassroomAccessSupport.blank(item.getDescription(), "请提交成果物。"));
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
                        ClassroomAccessSupport.blank(item.getTitle(), "子任务 " + (i + 1)),
                        ClassroomAccessSupport.blank(item.getDescription(), "请提交成果物。"));
            } else {
                nextSubSeq++;
                ClassroomSubTaskEntity sub = new ClassroomSubTaskEntity();
                sub.setTaskId(taskId);
                sub.setSubTaskId("SUB-" + String.format("%03d", nextSubSeq));
                sub.setOrderNo(i + 1);
                sub.setTitle(ClassroomAccessSupport.blank(item.getTitle(), "子任务 " + (i + 1)));
                sub.setDescription(ClassroomAccessSupport.blank(item.getDescription(), "请提交成果物。"));
                subTaskMapper.insert(sub);
            }
        }
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
}
