package com.neusoft.edu.neullmdev.controller.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.dto.classroom.GradeSubmissionRequest;
import com.neusoft.edu.neullmdev.dto.classroom.ParseTaskTextRequest;
import com.neusoft.edu.neullmdev.dto.classroom.SendStudentReminderRequest;
import com.neusoft.edu.neullmdev.dto.classroom.UpsertStudentRequest;
import com.neusoft.edu.neullmdev.dto.classroom.TaskDraftAssistRequest;
import com.neusoft.edu.neullmdev.model.api.ApiResponse;
import com.neusoft.edu.neullmdev.model.classroom.ParsedTaskDocument;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomDashboardService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomReminderService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskTemplateService;
import com.neusoft.edu.neullmdev.service.classroom.SubmissionGradingAssistService;
import com.neusoft.edu.neullmdev.service.classroom.TaskDocumentParserService;
import com.neusoft.edu.neullmdev.service.classroom.TaskDraftAssistService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomDashboardService dashboardService;
    private final SubmissionGradingAssistService gradingAssistService;
    private final TaskDocumentParserService taskDocumentParserService;
    private final ClassroomTaskTemplateService taskTemplateService;
    private final ClassroomReminderService reminderService;
    private final TaskDraftAssistService taskDraftAssistService;

    public ClassroomController(ClassroomService classroomService,
                               ClassroomDashboardService dashboardService,
                               SubmissionGradingAssistService gradingAssistService,
                               TaskDocumentParserService taskDocumentParserService,
                               ClassroomTaskTemplateService taskTemplateService,
                               ClassroomReminderService reminderService,
                               TaskDraftAssistService taskDraftAssistService) {
        this.classroomService = classroomService;
        this.dashboardService = dashboardService;
        this.gradingAssistService = gradingAssistService;
        this.taskDocumentParserService = taskDocumentParserService;
        this.taskTemplateService = taskTemplateService;
        this.reminderService = reminderService;
        this.taskDraftAssistService = taskDraftAssistService;
    }

    @GetMapping("/students")
    public ApiResponse<List<Map<String, Object>>> students() {
        return ApiResponse.success(classroomService.listStudents(null));
    }

    @GetMapping("/students/scope-options")
    public ApiResponse<Map<String, Object>> studentScopeOptions() {
        return ApiResponse.success(classroomService.listStudentScopeOptions());
    }

    @GetMapping("/students/{studentUserId}")
    public ApiResponse<Map<String, Object>> getStudent(@PathVariable Long studentUserId) {
        return ApiResponse.success(classroomService.getStudent(studentUserId));
    }

    @PostMapping("/students")
    public ApiResponse<Map<String, Object>> createStudent(@RequestBody UpsertStudentRequest request) {
        return ApiResponse.success(classroomService.createStudent(request));
    }

    @PutMapping("/students/{studentUserId}")
    public ApiResponse<Map<String, Object>> updateStudent(@PathVariable Long studentUserId,
                                                          @RequestBody UpsertStudentRequest request) {
        return ApiResponse.success(classroomService.updateStudent(studentUserId, request));
    }

    @DeleteMapping("/students/{studentUserId}")
    public ApiResponse<Map<String, Object>> deleteStudent(@PathVariable Long studentUserId) {
        classroomService.deleteStudent(studentUserId);
        return ApiResponse.success("学生已删除", Map.of());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<Map<String, Object>>> teacherTasks() {
        return ApiResponse.success(classroomService.listTasksForTeacher());
    }

    @PostMapping("/tasks")
    public ApiResponse<Map<String, Object>> createTask(@RequestBody CreateTaskRequest request) {
        return ApiResponse.success(classroomService.createTask(request));
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable String taskId) {
        return ApiResponse.success(classroomService.getTaskForTeacher(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ApiResponse<Map<String, Object>> updateTask(@PathVariable String taskId,
                                                       @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(classroomService.updateTask(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Map<String, Object>> deleteTask(@PathVariable String taskId) {
        classroomService.deleteTask(taskId);
        return ApiResponse.success("任务已删除", Map.of());
    }

    @GetMapping("/task-templates")
    public ApiResponse<List<Map<String, Object>>> taskTemplates() {
        return ApiResponse.success(taskTemplateService.listTemplates());
    }

    @GetMapping("/task-templates/{templateId}")
    public ApiResponse<Map<String, Object>> taskTemplate(@PathVariable String templateId) {
        return ApiResponse.success(taskTemplateService.getTemplate(templateId));
    }

    @PostMapping("/parse-task-document")
    public ApiResponse<ParsedTaskDocument> parseTaskDocument(@RequestParam MultipartFile file) throws IOException {
        return ApiResponse.success(taskDocumentParserService.parse(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.txt",
                file.getBytes()));
    }

    @PostMapping("/parse-task-text")
    public ApiResponse<ParsedTaskDocument> parseTaskText(@RequestBody ParseTaskTextRequest request) {
        String name = request.getFileName() != null ? request.getFileName() : "paste.txt";
        return ApiResponse.success(taskDocumentParserService.parseText(name, request.getText()));
    }

    @PostMapping("/task-draft-assist")
    public ApiResponse<Map<String, Object>> taskDraftAssist(@RequestBody TaskDraftAssistRequest request) {
        return ApiResponse.success(taskDraftAssistService.assist(request));
    }

    @PostMapping("/tasks/{taskId}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable String taskId) {
        return ApiResponse.success(classroomService.publishTask(taskId));
    }

    @PostMapping("/tasks/{taskId}/send-reminders")
    public ApiResponse<Map<String, Object>> sendReminders(@PathVariable String taskId) {
        return ApiResponse.success(reminderService.sendReminders(taskId));
    }

    @PostMapping("/tasks/{taskId}/send-reminder")
    public ApiResponse<Map<String, Object>> sendStudentReminder(@PathVariable String taskId,
                                                                @RequestBody SendStudentReminderRequest request) {
        return ApiResponse.success(reminderService.sendReminderToStudent(
                taskId, request.getStudentUserId(), request.getSubTaskId()));
    }

    @GetMapping("/tasks/{taskId}/dashboard")
    public ApiResponse<ProgressDashboard> dashboard(@PathVariable String taskId) {
        return ApiResponse.success(dashboardService.buildDashboard(taskId));
    }

    @GetMapping("/tasks/{taskId}/submissions")
    public ApiResponse<List<Map<String, Object>>> submissions(@PathVariable String taskId) {
        return ApiResponse.success(classroomService.listSubmissions(taskId));
    }

    @GetMapping("/my-assignments")
    public ApiResponse<List<Map<String, Object>>> myAssignments() {
        return ApiResponse.success(classroomService.listMyAssignments());
    }

    @PostMapping("/submit-file")
    public ApiResponse<Map<String, Object>> submitFile(@RequestParam String taskId,
                                                       @RequestParam String subTaskId,
                                                       @RequestParam MultipartFile file) throws IOException {
        return ApiResponse.success("提交成功", classroomService.submitFile(taskId, subTaskId, file));
    }

    @PostMapping("/submit")
    public ApiResponse<Map<String, Object>> submitText(@RequestParam String taskId,
                                                       @RequestParam String subTaskId,
                                                       @RequestParam(required = false) String fileName,
                                                       @RequestParam String content) {
        return ApiResponse.success(classroomService.submitText(taskId, subTaskId, fileName, content));
    }

    @PostMapping("/submissions/{submissionId}/grading-assist")
    public ApiResponse<Map<String, Object>> gradingAssist(@PathVariable String submissionId) {
        return ApiResponse.success(gradingAssistService.assist(submissionId));
    }

    @PostMapping("/grade")
    public ApiResponse<Map<String, Object>> grade(@RequestBody GradeSubmissionRequest request) {
        return ApiResponse.success(classroomService.gradeSubmission(
                request.getSubmissionId(), request.getScore(), request.getComment()));
    }

    @PostMapping("/reject")
    public ApiResponse<Map<String, Object>> reject(@RequestBody GradeSubmissionRequest request) {
        return ApiResponse.success(classroomService.rejectSubmission(
                request.getSubmissionId(), request.getComment()));
    }
}
