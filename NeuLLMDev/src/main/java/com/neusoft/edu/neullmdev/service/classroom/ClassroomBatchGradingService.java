package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassroomBatchGradingService {

    private static final int MAX_BATCH = 8;

    private final ClassroomService classroomService;
    private final SubmissionGradingAssistService gradingAssistService;

    public ClassroomBatchGradingService(ClassroomService classroomService,
                                          SubmissionGradingAssistService gradingAssistService) {
        this.classroomService = classroomService;
        this.gradingAssistService = gradingAssistService;
    }

    public Map<String, Object> batchAssist(String taskId) {
        classroomService.requireTeacher();
        List<Map<String, Object>> all = classroomService.listSubmissions(taskId);
        List<Map<String, Object>> pending = all.stream()
                .filter(s -> SubmissionStatus.SUBMITTED.name().equals(String.valueOf(s.get("status"))))
                .toList();
        List<Map<String, Object>> results = new ArrayList<>();
        int limit = Math.min(MAX_BATCH, pending.size());
        for (int i = 0; i < limit; i++) {
            Map<String, Object> sub = pending.get(i);
            String submissionId = String.valueOf(sub.get("submissionId"));
            try {
                Map<String, Object> assist = gradingAssistService.assist(submissionId);
                Map<String, Object> row = new LinkedHashMap<>(assist);
                row.put("studentName", sub.get("studentName"));
                row.put("studentNo", sub.get("studentNo"));
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
