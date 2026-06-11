package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.UserRole;
import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.dto.classroom.response.TaskSubmissionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** 教师端对话：在用户消息末尾注入班级作业与提交上下文，便于模型选对 taskId。 */
@Service
public class TeacherChatContextService {

    private final ClassroomTaskService taskService;
    private final ClassroomSubmissionService submissionService;

    public TeacherChatContextService(ClassroomTaskService taskService,
                                     ClassroomSubmissionService submissionService) {
        this.taskService = taskService;
        this.submissionService = submissionService;
    }

    public String enrich(String userMessage, boolean teacherMode) {
        if (!teacherMode || userMessage == null) {
            return userMessage;
        }
        try {
            if (AuthContext.get() == null || AuthContext.require().role() != UserRole.TEACHER) {
                return userMessage;
            }
            List<ClassroomTaskResponse> tasks = taskService.listTasksForTeacher();
            StringBuilder ctx = new StringBuilder("\n\n【教师端上下文】\n");
            if (tasks.isEmpty()) {
                ctx.append("当前尚无课堂作业；创建作业请调用 create_classroom_task。\n");
            } else {
                ctx.append("已有作业（工具参数请使用下列 taskId）：\n");
                String latestPublished = null;
                String latestAny = null;
                for (ClassroomTaskResponse t : tasks) {
                    String id = t.taskId();
                    String title = t.title();
                    boolean pub = t.published();
                    ctx.append("- ").append(id).append(" 《").append(title).append("》")
                            .append(pub ? " [已发布]" : " [草稿]").append('\n');
                    latestAny = id;
                    if (pub) {
                        latestPublished = id;
                    }
                }
                String hint = latestPublished != null ? latestPublished : latestAny;
                if (hint != null) {
                    ctx.append("最新可参考作业 taskId：").append(hint).append('\n');
                    appendSubmissionHints(ctx, hint);
                }
            }
            return userMessage + ctx;
        } catch (Exception e) {
            return userMessage;
        }
    }

    private void appendSubmissionHints(StringBuilder ctx, String taskId) {
        try {
            List<TaskSubmissionResponse> subs = submissionService.listSubmissions(taskId);
            if (subs.isEmpty()) {
                ctx.append("该任务暂无学生提交。\n");
                return;
            }
            ctx.append("待批改/最近提交（assist_grade_submission 用 submissionId）：\n");
            int n = 0;
            for (TaskSubmissionResponse s : subs) {
                if (n >= 8) {
                    ctx.append("… 共 ").append(subs.size()).append(" 条提交\n");
                    break;
                }
                String sid = s.submissionId();
                if (sid != null) {
                    ctx.append("  · ").append(sid);
                    if (s.studentName() != null) {
                        ctx.append(" ").append(s.studentName());
                    }
                    if (s.status() != null) {
                        ctx.append(" [").append(s.status()).append(']');
                    }
                    ctx.append('\n');
                    n++;
                }
            }
        } catch (Exception ignored) {
            // 忽略提交列表加载失败
        }
    }
}
