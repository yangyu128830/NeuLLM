package com.neusoft.edu.neullmdev.controller.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.CreateTaskRequest;
import com.neusoft.edu.neullmdev.dto.classroom.GradeSubmissionRequest;
import com.neusoft.edu.neullmdev.dto.classroom.ParseTaskTextRequest;
import com.neusoft.edu.neullmdev.dto.classroom.SendStudentReminderRequest;
import com.neusoft.edu.neullmdev.dto.classroom.UpsertStudentRequest;
import com.neusoft.edu.neullmdev.dto.classroom.TaskDraftAssistRequest;
import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentAssignmentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.StudentScopeOptionsResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.SubmissionResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import com.neusoft.edu.neullmdev.model.api.ApiResponse;
import com.neusoft.edu.neullmdev.model.classroom.ParsedTaskDocument;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.service.classroom.AssignmentSummaryService;
import com.neusoft.edu.neullmdev.service.classroom.TeacherClassInsightSummaryService;
import com.neusoft.edu.neullmdev.service.classroom.TeacherSubmissionsSummaryService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomDashboardService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomReminderService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomStudentService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomSubmissionService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
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

    private final ClassroomStudentService studentService;
    private final ClassroomTaskService taskService;
    private final ClassroomSubmissionService submissionService;
    private final ClassroomDashboardService dashboardService;
    private final SubmissionGradingAssistService gradingAssistService;
    private final TaskDocumentParserService taskDocumentParserService;
    private final ClassroomTaskTemplateService taskTemplateService;
    private final ClassroomReminderService reminderService;
    private final TaskDraftAssistService taskDraftAssistService;
    private final AssignmentSummaryService assignmentSummaryService;
    private final TeacherSubmissionsSummaryService teacherSubmissionsSummaryService;
    private final TeacherClassInsightSummaryService teacherClassInsightSummaryService;

    public ClassroomController(ClassroomStudentService studentService,
                               ClassroomTaskService taskService,
                               ClassroomSubmissionService submissionService,
                               ClassroomDashboardService dashboardService,
                               SubmissionGradingAssistService gradingAssistService,
                               TaskDocumentParserService taskDocumentParserService,
                               ClassroomTaskTemplateService taskTemplateService,
                               ClassroomReminderService reminderService,
                               TaskDraftAssistService taskDraftAssistService,
                               AssignmentSummaryService assignmentSummaryService,
                               TeacherSubmissionsSummaryService teacherSubmissionsSummaryService,
                               TeacherClassInsightSummaryService teacherClassInsightSummaryService) {
        this.studentService = studentService;
        this.taskService = taskService;
        this.submissionService = submissionService;
        this.dashboardService = dashboardService;
        this.gradingAssistService = gradingAssistService;
        this.taskDocumentParserService = taskDocumentParserService;
        this.taskTemplateService = taskTemplateService;
        this.reminderService = reminderService;
        this.taskDraftAssistService = taskDraftAssistService;
        this.assignmentSummaryService = assignmentSummaryService;
        this.teacherSubmissionsSummaryService = teacherSubmissionsSummaryService;
        this.teacherClassInsightSummaryService = teacherClassInsightSummaryService;
    }

    @GetMapping("/students")
    public ApiResponse<List<StudentResponse>> students() {
        return ApiResponse.success(studentService.listStudents(null));
    }

    @GetMapping("/students/scope-options")
    public ApiResponse<StudentScopeOptionsResponse> studentScopeOptions() {
        return ApiResponse.success(studentService.listStudentScopeOptions());
    }

    @GetMapping("/students/{studentUserId}")
    public ApiResponse<StudentResponse> getStudent(@PathVariable Long studentUserId) {
        return ApiResponse.success(studentService.getStudent(studentUserId));
    }

    @PostMapping("/students")
    public ApiResponse<StudentResponse> createStudent(@RequestBody UpsertStudentRequest request) {
        return ApiResponse.success(studentService.createStudent(request));
    }

    @PutMapping("/students/{studentUserId}")
    public ApiResponse<StudentResponse> updateStudent(@PathVariable Long studentUserId,
                                                      @RequestBody UpsertStudentRequest request) {
        return ApiResponse.success(studentService.updateStudent(studentUserId, request));
    }

    @DeleteMapping("/students/{studentUserId}")
    public ApiResponse<Map<String, Object>> deleteStudent(@PathVariable Long studentUserId) {
        studentService.deleteStudent(studentUserId);
        return ApiResponse.success("学生已删除", Map.of());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<ClassroomTaskResponse>> teacherTasks() {
        return ApiResponse.success(taskService.listTasksForTeacher());
    }

    @PostMapping("/tasks")
    public ApiResponse<ClassroomTaskResponse> createTask(@RequestBody CreateTaskRequest request) {
        return ApiResponse.success(taskService.createTask(request));
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<ClassroomTaskResponse> getTask(@PathVariable String taskId) {
        return ApiResponse.success(taskService.getTaskForTeacher(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ApiResponse<ClassroomTaskResponse> updateTask(@PathVariable String taskId,
                                                         @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(taskService.updateTask(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Map<String, Object>> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId);
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
    public ApiResponse<ClassroomTaskResponse> publish(@PathVariable String taskId) {
        return ApiResponse.success(taskService.publishTask(taskId));
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
    public ApiResponse<List<TaskSubmissionResponse>> submissions(@PathVariable String taskId) {
        return ApiResponse.success(submissionService.listSubmissions(taskId));
    }

    @GetMapping("/submissions/recent-summary")
    public ApiResponse<Map<String, Object>> recentSubmissionsSummary() {
        return ApiResponse.success(teacherSubmissionsSummaryService.summarizeForCurrentTeacher());
    }

    @GetMapping("/class-insight/summary")
    public ApiResponse<Map<String, Object>> classInsightSummary(
            @RequestParam(required = false) String focus,
            @RequestParam(required = false) String question) {
        return ApiResponse.success(teacherClassInsightSummaryService.summarize(focus, question));
    }

    @GetMapping("/my-assignments/summary")
    public ApiResponse<Map<String, Object>> myAssignmentsSummary() {
        return ApiResponse.success(assignmentSummaryService.summarizeForCurrentStudent());
    }

    @GetMapping("/my-assignments")
    public ApiResponse<List<StudentAssignmentResponse>> myAssignments() {
        return ApiResponse.success(taskService.listMyAssignments());
    }

    @PostMapping("/submit-file")
    public ApiResponse<SubmissionResponse> submitFile(@RequestParam String taskId,
                                                      @RequestParam String subTaskId,
                                                      @RequestParam MultipartFile file) throws IOException {
        return ApiResponse.success("提交成功", submissionService.submitFile(taskId, subTaskId, file));
    }

    @PostMapping("/submit")
    public ApiResponse<SubmissionResponse> submitText(@RequestParam String taskId,
                                                        @RequestParam String subTaskId,
                                                        @RequestParam(required = false) String fileName,
                                                        @RequestParam String content) {
        return ApiResponse.success(submissionService.submitText(taskId, subTaskId, fileName, content));
    }

    @PostMapping("/submissions/{submissionId}/grading-assist")
    public ApiResponse<Map<String, Object>> gradingAssist(@PathVariable String submissionId) {
        return ApiResponse.success(gradingAssistService.assist(submissionId));
    }

    @PostMapping("/grade")
    public ApiResponse<SubmissionResponse> grade(@RequestBody GradeSubmissionRequest request) {
        return ApiResponse.success(submissionService.gradeSubmission(
                request.getSubmissionId(), request.getScore(), request.getComment()));
    }

    @PostMapping("/reject")
    public ApiResponse<SubmissionResponse> reject(@RequestBody GradeSubmissionRequest request) {
        return ApiResponse.success(submissionService.rejectSubmission(
                request.getSubmissionId(), request.getComment()));
    }
}
