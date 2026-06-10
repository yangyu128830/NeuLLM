package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubTaskEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomSubmissionEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskTargeting;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubTaskMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomSubmissionMapper;
import com.neusoft.edu.neullmdev.mapper.classroom.ClassroomTaskMapper;
import com.neusoft.edu.neullmdev.dto.auth.TeachingScopeItem;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.service.auth.TeacherTeachingScopeUtil;
import com.neusoft.edu.neullmdev.model.classroom.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassroomDashboardService {

    private final ClassroomTaskMapper taskMapper;
    private final ClassroomSubTaskMapper subTaskMapper;
    private final ClassroomSubmissionMapper submissionMapper;
    private final SysUserMapper sysUserMapper;

    public ClassroomDashboardService(ClassroomTaskMapper taskMapper,
                                     ClassroomSubTaskMapper subTaskMapper,
                                     ClassroomSubmissionMapper submissionMapper,
                                     SysUserMapper sysUserMapper) {
        this.taskMapper = taskMapper;
        this.subTaskMapper = subTaskMapper;
        this.submissionMapper = submissionMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public ProgressDashboard buildDashboard(String taskId) {
        com.neusoft.edu.neullmdev.auth.AuthContext.require();
        if (com.neusoft.edu.neullmdev.auth.AuthContext.require().role()
                != com.neusoft.edu.neullmdev.auth.UserRole.TEACHER) {
            throw new IllegalArgumentException("仅教师可查看进度看板");
        }
        ClassroomTaskEntity task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        List<ClassroomSubTaskEntity> subTasks = subTaskMapper.listByTask(taskId);
        SysUserEntity teacherUser = sysUserMapper.findById(AuthContext.require().id());
        List<TeachingScopeItem> teacherScopes = teacherUser == null
                ? List.of() : TeacherTeachingScopeUtil.parseScopes(teacherUser.getTeachingScopes());
        List<SysUserEntity> students = sysUserMapper.listStudentsByClass(task.getClassId()).stream()
                .filter(s -> TeacherTeachingScopeUtil.studentAccessible(s, teacherScopes))
                .filter(s -> ClassroomTaskTargeting.matches(task, s))
                .toList();

        int submittedCount = 0;
        int availableCount = 0;
        int lockedCount = 0;
        List<Map<String, Object>> rows = new ArrayList<>();

        for (SysUserEntity student : students) {
            List<Map<String, Object>> cells = new ArrayList<>();
            for (ClassroomSubTaskEntity sub : subTasks) {
                ClassroomSubmissionEntity subm = submissionMapper.findCell(
                        taskId, sub.getSubTaskId(), student.getId());
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("subTaskId", sub.getSubTaskId());
                cell.put("title", sub.getTitle());
                if (subm != null) {
                    submittedCount++;
                    String state = mapState(subm.getStatus());
                    cell.put("state", state);
                    cell.put("label", labelFor(state, subm));
                    cell.put("submissionId", subm.getSubmissionId());
                    cell.put("fileName", subm.getFileName());
                    cell.put("score", subm.getScore());
                } else if (canSubmit(taskId, sub, student.getId())) {
                    availableCount++;
                    cell.put("state", "AVAILABLE");
                    cell.put("label", "可提交");
                } else {
                    lockedCount++;
                    cell.put("state", "LOCKED");
                    cell.put("label", "未解锁");
                }
                cells.add(cell);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", student.getStudentNo());
            row.put("studentUserId", student.getId());
            row.put("studentName", student.getDisplayName());
            row.put("cells", cells);
            rows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("studentCount", rows.size());
        summary.put("subTaskCount", subTasks.size());
        summary.put("submittedCount", submittedCount);
        summary.put("availableCount", availableCount);
        summary.put("lockedCount", lockedCount);
        summary.put("reminderCount", countReminders(rows, subTasks.size()));

        List<Map<String, Object>> subTaskVos = subTasks.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("subTaskId", s.getSubTaskId());
            m.put("title", s.getTitle());
            return m;
        }).toList();

        return new ProgressDashboard(taskId, task.getTitle(), subTaskVos, rows, summary);
    }

    private String mapState(String status) {
        if (SubmissionStatus.GRADED.name().equals(status)) {
            return "GRADED";
        }
        if (SubmissionStatus.REJECTED.name().equals(status)) {
            return "NEEDS_REVISION";
        }
        return "SUBMITTED";
    }

    private String labelFor(String state, ClassroomSubmissionEntity subm) {
        return switch (state) {
            case "GRADED" -> "已批改" + (subm.getScore() != null ? " " + subm.getScore() + "分" : "");
            case "NEEDS_REVISION" -> "需重交";
            default -> "已提交";
        };
    }

    private boolean canSubmit(String taskId, ClassroomSubTaskEntity subTask, Long studentUserId) {
        ClassroomSubmissionEntity existing = submissionMapper.findCell(
                taskId, subTask.getSubTaskId(), studentUserId);
        if (existing != null && SubmissionStatus.REJECTED.name().equals(existing.getStatus())) {
            return true;
        }
        if (existing != null) {
            return false;
        }
        if (subTask.getOrderNo() == 1) {
            return true;
        }
        return subTaskMapper.listByTask(taskId).stream()
                .filter(s -> s.getOrderNo() == subTask.getOrderNo() - 1)
                .findFirst()
                .map(p -> submissionMapper.findCell(taskId, p.getSubTaskId(), studentUserId) != null)
                .orElse(true);
    }

    private int countReminders(List<Map<String, Object>> rows, int subTaskCount) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) row.get("cells");
            long done = cells.stream().filter(c -> !"AVAILABLE".equals(c.get("state")) && !"LOCKED".equals(c.get("state"))).count();
            if (done < subTaskCount) {
                count++;
            }
        }
        return count;
    }
}
