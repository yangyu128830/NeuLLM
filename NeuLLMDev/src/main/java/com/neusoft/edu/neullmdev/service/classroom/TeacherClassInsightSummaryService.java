package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.response.ClassroomTaskResponse;
import com.neusoft.edu.neullmdev.model.classroom.ProgressDashboard;
import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TeacherClassInsightSummaryService {

    private final ClassroomAccessSupport access;
    private final ClassroomTaskService taskService;
    private final ClassroomDashboardService dashboardService;
    private final KimiChatService kimiChatService;

    public TeacherClassInsightSummaryService(ClassroomAccessSupport access,
                                             ClassroomTaskService taskService,
                                             ClassroomDashboardService dashboardService,
                                             KimiChatService kimiChatService) {
        this.access = access;
        this.taskService = taskService;
        this.dashboardService = dashboardService;
        this.kimiChatService = kimiChatService;
    }

    public Map<String, Object> summarize(String focus, String question) {
        access.requireTeacher();
        String effectiveFocus = normalizeFocus(focus);
        List<ClassroomTaskResponse> tasks = taskService.listTasksForTeacher();
        List<ClassroomTaskResponse> published = tasks.stream()
                .filter(ClassroomTaskResponse::published)
                .toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("focus", effectiveFocus);
        out.put("focusLabel", focusLabel(effectiveFocus));
        out.put("publishedTaskCount", published.size());
        out.put("empty", published.isEmpty());

        if (published.isEmpty()) {
            out.put("summary", fallbackEmpty());
            out.put("highlightStudents", List.of());
            out.put("unsubmittedCount", 0);
            out.put("topPerformerCount", 0);
            out.put("underperformerCount", 0);
            out.put("followUpCount", 0);
            out.put("classCompletionPercent", 0);
            return out;
        }

        Map<String, StudentInsight> students = new LinkedHashMap<>();
        String latestTaskId = null;
        String latestTaskTitle = null;
        int totalCells = 0;
        int doneCells = 0;

        for (ClassroomTaskResponse task : published) {
            String taskId = task.taskId();
            String taskTitle = task.title() != null ? task.title() : taskId;
            latestTaskId = taskId;
            latestTaskTitle = taskTitle;

            ProgressDashboard dash = dashboardService.buildDashboard(taskId);
            for (Map<String, Object> row : dash.rows()) {
                String studentId = String.valueOf(row.get("studentId"));
                StudentInsight insight = students.computeIfAbsent(studentId, id -> new StudentInsight(row));
                TaskSlice slice = sliceForRow(taskId, taskTitle, row);
                insight.taskSlices.add(slice);
                totalCells += slice.totalCells;
                doneCells += slice.doneCells;
            }
        }

        for (StudentInsight insight : students.values()) {
            insight.finalizeStats();
        }

        List<Map<String, Object>> unsubmitted = students.values().stream()
                .filter(s -> s.pendingCells > 0)
                .sorted(Comparator.comparingInt((StudentInsight s) -> s.pendingCells).reversed()
                        .thenComparing(s -> s.studentName, Comparator.nullsLast(String::compareTo)))
                .map(this::toHighlightMap)
                .toList();

        List<Map<String, Object>> performers = students.values().stream()
                .filter(s -> s.overallPercent >= 100 || (s.avgScore != null && s.avgScore >= 85))
                .sorted(Comparator
                        .comparingInt((StudentInsight s) -> s.overallPercent).reversed()
                        .thenComparing((StudentInsight s) -> s.avgScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toHighlightMap)
                .toList();

        List<Map<String, Object>> followUp = students.values().stream()
                .filter(s -> s.overallPercent > 0 && s.overallPercent < 100)
                .sorted(Comparator.comparingInt((StudentInsight s) -> s.overallPercent))
                .map(this::toHighlightMap)
                .toList();

        List<Map<String, Object>> underperformers = students.values().stream()
                .sorted(Comparator
                        .comparingInt((StudentInsight s) -> s.overallPercent)
                        .thenComparing((StudentInsight s) -> s.avgScore, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Comparator.comparingInt((StudentInsight s) -> s.pendingCells).reversed()))
                .map(this::toHighlightMap)
                .toList();

        int classPct = totalCells == 0 ? 0 : Math.round((doneCells * 100f) / totalCells);
        List<Map<String, Object>> highlights = pickHighlights(
                effectiveFocus, unsubmitted, performers, followUp, underperformers);

        String summary = composeSummary(
                effectiveFocus,
                question,
                published.size(),
                classPct,
                unsubmitted.size(),
                performers.size(),
                underperformers.size(),
                followUp.size(),
                highlights,
                latestTaskTitle);

        out.put("summary", summary);
        out.put("highlightStudents", highlights);
        out.put("unsubmittedCount", unsubmitted.size());
        out.put("topPerformerCount", performers.size());
        out.put("underperformerCount", underperformers.size());
        out.put("followUpCount", followUp.size());
        out.put("classCompletionPercent", classPct);
        out.put("latestTaskId", latestTaskId);
        out.put("latestTaskTitle", latestTaskTitle);
        return out;
    }

    private static String normalizeFocus(String focus) {
        if (!StringUtils.hasText(focus)) {
            return "general";
        }
        return switch (focus.trim().toLowerCase()) {
            case "unsubmitted", "pending", "remind" -> "unsubmitted";
            case "performers", "top", "excellent" -> "performers";
            case "underperformers", "worst", "bottom", "poor" -> "underperformers";
            case "follow_up", "followup", "follow" -> "follow_up";
            default -> "general";
        };
    }

    private static String focusLabel(String focus) {
        return switch (focus) {
            case "unsubmitted" -> "待交作业";
            case "performers" -> "表现突出";
            case "underperformers" -> "待提升";
            case "follow_up" -> "需跟进";
            default -> "班级学情";
        };
    }

    private TaskSlice sliceForRow(String taskId, String taskTitle, Map<String, Object> row) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) row.getOrDefault("cells", List.of());
        int pending = 0;
        int done = 0;
        double scoreSum = 0;
        int scoreCount = 0;
        String statusLabel = "已完成";

        for (Map<String, Object> cell : cells) {
            String state = String.valueOf(cell.getOrDefault("state", ""));
            if ("AVAILABLE".equals(state) || "NEEDS_REVISION".equals(state)) {
                pending++;
                statusLabel = "NEEDS_REVISION".equals(state) ? "需重交" : "待提交";
            } else if ("LOCKED".equals(state)) {
                // locked cells are not counted as done
            } else {
                done++;
                if ("GRADED".equals(state)) {
                    Object score = cell.get("score");
                    if (score instanceof Number n) {
                        scoreSum += n.doubleValue();
                        scoreCount++;
                    }
                }
            }
        }

        Integer avgScore = scoreCount > 0 ? (int) Math.round(scoreSum / scoreCount) : null;
        return new TaskSlice(taskId, taskTitle, cells.size(), done, pending, avgScore, statusLabel);
    }

    private Map<String, Object> toHighlightMap(StudentInsight s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("studentId", s.studentId);
        m.put("studentName", s.studentName);
        m.put("studentUserId", s.studentUserId);
        m.put("taskId", s.primaryTaskId);
        m.put("taskTitle", s.primaryTaskTitle);
        m.put("statusLabel", s.statusLabel);
        m.put("completionPercent", s.overallPercent);
        m.put("pendingCellCount", s.pendingCells);
        if (s.avgScore != null) {
            m.put("avgScore", s.avgScore);
        }
        return m;
    }

    private static List<Map<String, Object>> pickHighlights(
            String focus,
            List<Map<String, Object>> unsubmitted,
            List<Map<String, Object>> performers,
            List<Map<String, Object>> followUp,
            List<Map<String, Object>> underperformers) {
        return switch (focus) {
            case "unsubmitted" -> unsubmitted.stream().limit(12).toList();
            case "performers" -> performers.stream().limit(12).toList();
            case "underperformers" -> underperformers.stream().limit(12).toList();
            case "follow_up" -> followUp.stream().limit(12).toList();
            default -> {
                List<Map<String, Object>> mix = new ArrayList<>();
                mix.addAll(unsubmitted.stream().limit(4).toList());
                mix.addAll(performers.stream().limit(4).toList());
                yield mix;
            }
        };
    }

    private String composeSummary(
            String focus,
            String question,
            int taskCount,
            int classPct,
            int unsubmittedCount,
            int performerCount,
            int underperformerCount,
            int followUpCount,
            List<Map<String, Object>> highlights,
            String latestTaskTitle) {
        try {
            JSONObject payload = new JSONObject()
                    .put("focus", focus)
                    .put("focusLabel", focusLabel(focus))
                    .put("teacherQuestion", question == null ? "" : question.trim())
                    .put("publishedTaskCount", taskCount)
                    .put("classCompletionPercent", classPct)
                    .put("unsubmittedCount", unsubmittedCount)
                    .put("topPerformerCount", performerCount)
                    .put("underperformerCount", underperformerCount)
                    .put("followUpCount", followUpCount)
                    .put("latestTaskTitle", latestTaskTitle == null ? "" : latestTaskTitle);
            JSONArray arr = new JSONArray();
            for (Map<String, Object> item : highlights.stream().limit(8).toList()) {
                JSONObject o = new JSONObject()
                        .put("studentName", item.getOrDefault("studentName", ""))
                        .put("studentId", item.getOrDefault("studentId", ""))
                        .put("taskTitle", item.getOrDefault("taskTitle", ""))
                        .put("statusLabel", item.getOrDefault("statusLabel", ""))
                        .put("completionPercent", item.getOrDefault("completionPercent", 0))
                        .put("pendingCellCount", item.getOrDefault("pendingCellCount", 0));
                if (item.containsKey("avgScore")) {
                    o.put("avgScore", item.get("avgScore"));
                }
                arr.put(o);
            }
            payload.put("highlightStudents", arr);

            String system = buildSystemPrompt(focus);
            String raw = kimiChatService.chatWithSystem(system, payload.toString(), 0.45)
                    .block(Duration.ofSeconds(45));
            if (raw != null && !raw.isBlank() && !raw.startsWith("解析响应出错")) {
                return raw.trim();
            }
        } catch (Exception e) {
            log.warn("教师学情 AI 总结失败，使用规则兜底: {}", e.getMessage());
        }
        return fallbackSummary(focus, classPct, unsubmittedCount, performerCount, underperformerCount, followUpCount, highlights);
    }

    private static String buildSystemPrompt(String focus) {
        String focusHint = switch (focus) {
            case "unsubmitted" -> "重点回答谁还没交、涉及哪些作业，语气关切但不责备，可建议去学情看板催交。";
            case "performers" -> "重点表扬完成度高或分数好的学生，语气积极鼓励。";
            case "underperformers" -> "重点说明完成度较低或待交较多的学生，语气客观温和，避免贬低，并建议如何跟进帮助。";
            case "follow_up" -> "重点说明进行中学生与需关注原因，给出跟进建议。";
            default -> "概括班级整体完成度、待交与表现突出情况，并给 1 条可操作建议。";
        };
        return """
                你是「教学 Agent」，帮教师根据真实课堂学情数据回答提问。
                根据 JSON 中的 teacherQuestion、统计数据与 highlightStudents 写 2～4 句温暖、专业、口语化的回答。
                %s
                不要 markdown、不要编号列表；严禁编造 JSON 里没有的学生姓名或任务名。
                """.formatted(focusHint);
    }

    private static String fallbackEmpty() {
        return "目前还没有已发布的课堂作业～发布任务后，我才能帮你汇总谁交了、谁还没交、谁表现突出。";
    }

    private static String fallbackSummary(
            String focus,
            int classPct,
            int unsubmittedCount,
            int performerCount,
            int underperformerCount,
            int followUpCount,
            List<Map<String, Object>> highlights) {
        String firstName = highlights.isEmpty()
                ? "相关学生"
                : String.valueOf(highlights.get(0).getOrDefault("studentName", "学生"));
        return switch (focus) {
            case "unsubmitted" -> unsubmittedCount == 0
                    ? "目前暂无待交记录，班级整体完成度约 " + classPct + "%。"
                    : "有 " + unsubmittedCount + " 名学生仍有作业待交，建议优先关注 "
                    + firstName + "。可点击下方名单去学情看板催交。";
            case "performers" -> performerCount == 0
                    ? "暂时还没有完成度 100% 或高分的学生，班级整体完成度约 " + classPct + "%。"
                    : "有 " + performerCount + " 名学生表现突出，" + firstName + " 完成度较高，值得表扬～";
            case "underperformers" -> underperformerCount == 0
                    ? "班级整体完成度约 " + classPct + "% ，暂无明显落后学生。"
                    : "完成度相对较低的是 " + firstName + " 等 " + underperformerCount
                    + " 名同学，建议温和跟进、了解是否有困难。可点击下方查看详情。";
            case "follow_up" -> followUpCount == 0
                    ? "暂无进行中的学生，整体完成度约 " + classPct + "%。"
                    : "有 " + followUpCount + " 名学生仍在进行中，建议跟进 " + firstName + "。";
            default -> "班级整体完成度约 " + classPct + "%。待交 " + unsubmittedCount
                    + " 人，表现突出 " + performerCount + " 人，进行中 " + followUpCount + " 人。";
        };
    }

    private static final class TaskSlice {
        final String taskId;
        final String taskTitle;
        final int totalCells;
        final int doneCells;
        final int pendingCells;
        final Integer avgScore;
        final String statusLabel;

        TaskSlice(String taskId, String taskTitle, int totalCells, int doneCells, int pendingCells,
                  Integer avgScore, String statusLabel) {
            this.taskId = taskId;
            this.taskTitle = taskTitle;
            this.totalCells = totalCells;
            this.doneCells = doneCells;
            this.pendingCells = pendingCells;
            this.avgScore = avgScore;
            this.statusLabel = statusLabel;
        }
    }

    private static final class StudentInsight {
        final String studentId;
        final String studentName;
        final Long studentUserId;
        final List<TaskSlice> taskSlices = new ArrayList<>();
        int overallPercent;
        int pendingCells;
        Integer avgScore;
        String statusLabel = "已完成";
        String primaryTaskId = "";
        String primaryTaskTitle = "";

        StudentInsight(Map<String, Object> row) {
            this.studentId = String.valueOf(row.get("studentId"));
            this.studentName = String.valueOf(row.getOrDefault("studentName", studentId));
            Object uid = row.get("studentUserId");
            this.studentUserId = uid instanceof Number n ? n.longValue() : null;
        }

        void finalizeStats() {
            int total = 0;
            int done = 0;
            pendingCells = 0;
            double scoreSum = 0;
            int scoreCount = 0;
            TaskSlice primaryPending = null;

            for (TaskSlice slice : taskSlices) {
                total += slice.totalCells;
                done += slice.doneCells;
                pendingCells += slice.pendingCells;
                if (slice.pendingCells > 0 && (primaryPending == null
                        || slice.pendingCells > primaryPending.pendingCells)) {
                    primaryPending = slice;
                    statusLabel = slice.statusLabel;
                }
                if (slice.avgScore != null) {
                    scoreSum += slice.avgScore;
                    scoreCount++;
                }
            }

            overallPercent = total == 0 ? 0 : Math.round((done * 100f) / total);
            avgScore = scoreCount == 0 ? null : (int) Math.round(scoreSum / scoreCount);

            if (primaryPending != null) {
                primaryTaskId = primaryPending.taskId;
                primaryTaskTitle = primaryPending.taskTitle;
            } else if (!taskSlices.isEmpty()) {
                TaskSlice last = taskSlices.get(taskSlices.size() - 1);
                primaryTaskId = last.taskId;
                primaryTaskTitle = last.taskTitle;
                statusLabel = overallPercent >= 100 ? "已完成" : "进行中";
            }
        }
    }
}
