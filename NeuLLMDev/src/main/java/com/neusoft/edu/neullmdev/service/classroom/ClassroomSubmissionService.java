package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.dto.classroom.response.SubmissionResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import com.neusoft.edu.neullmdev.model.notification.NotificationType;
import com.neusoft.edu.neullmdev.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomSubmissionService {

    private final ClassroomAccessSupport access;
    private final ClassroomSubmissionMapper submissionMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final DocumentTextExtractor textExtractor;
    private final ArtifactStorageService artifactStorage;
    private final NotificationService notificationService;

    public ClassroomSubmissionService(ClassroomAccessSupport access,
                                      ClassroomSubmissionMapper submissionMapper,
                                      ClassroomSubTaskMapper subTaskMapper,
                                      DocumentTextExtractor textExtractor,
                                      ArtifactStorageService artifactStorage,
                                      NotificationService notificationService) {
        this.access = access;
        this.submissionMapper = submissionMapper;
        this.subTaskMapper = subTaskMapper;
        this.textExtractor = textExtractor;
        this.artifactStorage = artifactStorage;
        this.notificationService = notificationService;
    }

    @Transactional
    public SubmissionResponse submitFile(String taskId, String subTaskId, MultipartFile file) throws IOException {
        access.requireStudent();
        AuthUser student = AuthContext.require();
        ClassroomTaskEntity task = access.requirePublishedTask(taskId);
        ClassroomSubTaskEntity subTask = access.requireSubTask(taskId, subTaskId);
        if (!canSubmit(taskId, subTask, student.id())) {
            throw new IllegalArgumentException("前置子任务尚未提交，或当前状态不可提交");
        }
        byte[] bytes = file.getBytes();
        String content = textExtractor.extract(file.getOriginalFilename(), bytes);
        return saveSubmission(task, subTask, student, file.getOriginalFilename(), content, bytes);
    }

    @Transactional
    public SubmissionResponse submitText(String taskId, String subTaskId, String fileName, String content) {
        access.requireStudent();
        AuthUser student = AuthContext.require();
        ClassroomTaskEntity task = access.requirePublishedTask(taskId);
        ClassroomSubTaskEntity subTask = access.requireSubTask(taskId, subTaskId);
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
    public SubmissionResponse gradeSubmission(String submissionId, double score, String comment) {
        access.requireTeacher();
        access.requireSubmission(submissionId);
        submissionMapper.grade(submissionId, BigDecimal.valueOf(score), comment, AuthContext.require().id());
        ClassroomSubmissionEntity graded = submissionMapper.findById(submissionId);
        notificationService.notifyUser(
                graded.getStudentUserId(),
                NotificationType.GRADE_RESULT,
                "作业已批改",
                "您的提交已评分 " + score + " 分。" + (comment != null && !comment.isBlank() ? " 评语：" + comment : ""),
                "/assignments",
                graded.getTaskId());
        return ClassroomVoMapper.toSubmission(graded);
    }

    @Transactional
    public SubmissionResponse rejectSubmission(String submissionId, String comment) {
        access.requireTeacher();
        ClassroomSubmissionEntity sub = access.requireSubmission(submissionId);
        submissionMapper.reject(submissionId, comment, AuthContext.require().id());
        notificationService.notifyUser(
                sub.getStudentUserId(),
                NotificationType.TASK_REMIND,
                "作业需重新提交",
                (comment != null && !comment.isBlank() ? comment : "请根据教师意见修改后重新提交。"),
                "/assignments",
                sub.getTaskId());
        return ClassroomVoMapper.toSubmission(submissionMapper.findById(submissionId));
    }

    public List<TaskSubmissionResponse> listSubmissions(String taskId) {
        access.requireTeacher();
        access.requireTask(taskId);
        return submissionMapper.listByTask(taskId).stream()
                .map(s -> {
                    SubmissionResponse vo = ClassroomVoMapper.toSubmission(s);
                    SysUserEntity student = access.sysUserMapper().findById(s.getStudentUserId());
                    String studentName = student != null ? student.getDisplayName() : null;
                    String studentNo = student != null ? student.getStudentNo() : null;
                    return TaskSubmissionResponse.from(vo, studentName, studentNo);
                })
                .toList();
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

    private SubmissionResponse saveSubmission(ClassroomTaskEntity task,
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
            existing.setFileName(ClassroomAccessSupport.blank(fileName, subTask.getTitle() + ".txt"));
            existing.setContent(ClassroomAccessSupport.blank(content, ""));
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
            row.setFileName(ClassroomAccessSupport.blank(fileName, subTask.getTitle() + ".txt"));
            row.setContent(ClassroomAccessSupport.blank(content, ""));
            row.setStatus(SubmissionStatus.SUBMITTED.name());
            row.setSubmittedAt(LocalDateTime.now());
            if (bytes != null) {
                row.setArtifactPath(artifactStorage.store(submissionId, fileName, bytes));
            }
            submissionMapper.insert(row);
        }
        return ClassroomVoMapper.toSubmission(submissionMapper.findById(submissionId));
    }
}
