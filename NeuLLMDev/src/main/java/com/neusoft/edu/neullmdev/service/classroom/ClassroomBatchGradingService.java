package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassroomBatchGradingService {

    private static final int MAX_BATCH = 8;

    private final ClassroomAccessSupport access;
    private final ClassroomSubmissionService submissionService;
    private final SubmissionGradingAssistService gradingAssistService;

    public ClassroomBatchGradingService(ClassroomAccessSupport access,
                                        ClassroomSubmissionService submissionService,
                                        SubmissionGradingAssistService gradingAssistService) {
        this.access = access;
        this.submissionService = submissionService;
        this.gradingAssistService = gradingAssistService;
    }

    public Map<String, Object> batchAssist(String taskId) {
        access.requireTeacher();
        List<TaskSubmissionResponse> all = submissionService.listSubmissions(taskId);
        List<TaskSubmissionResponse> pending = all.stream()
                .filter(s -> SubmissionStatus.SUBMITTED.name().equals(s.status()))
                .toList();
        List<Map<String, Object>> results = new ArrayList<>();
        int limit = Math.min(MAX_BATCH, pending.size());
        for (int i = 0; i < limit; i++) {
            TaskSubmissionResponse sub = pending.get(i);
            String submissionId = sub.submissionId();
            try {
                Map<String, Object> assist = gradingAssistService.assist(submissionId);
                Map<String, Object> row = new LinkedHashMap<>(assist);
                row.put("studentName", sub.studentName());
                row.put("studentNo", sub.studentNo());
                results.add(row);
            } catch (Exception e) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("submissionId", submissionId);
                err.put("error", e.getMessage());
                results.add(err);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("totalPending", pending.size());
        out.put("processed", results.size());
        out.put("results", results);
        if (pending.size() > MAX_BATCH) {
            out.put("hint", "已处理前 " + MAX_BATCH + " 条，其余请在批改台继续。");
        }
        return out;
    }
}
