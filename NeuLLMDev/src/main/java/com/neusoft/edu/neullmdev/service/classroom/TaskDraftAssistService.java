package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.dto.classroom.TaskDraftAssistRequest;
import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师手动新建/编辑任务时的 AI 草稿辅助。
 */
@Slf4j
@Service
public class TaskDraftAssistService {

    private static final String SYSTEM_PROMPT = """
            你是「智学伴」课堂任务设计助手，帮助教师起草可发布的课堂作业草稿。
            根据教师的一句话需求（及可选的已有表单内容），生成结构清晰、可执行的任务方案。
            输出必须是单个 JSON 对象，不要 markdown 代码块，不要额外说明。字段如下：
            {
              "title": "任务标题，简洁明确，30字以内",
              "description": "向学生说明的任务目标、背景与总体要求，150-400字，分段清晰",
              "subTasks": [
                {"title": "子任务标题", "description": "该子任务的具体提交要求与成果物说明，40-150字"}
              ],
              "designNote": "给教师的一句话说明（如何布置、注意事项），60字以内"
            }
            子任务数量一般为 2-6 个；标题简短；描述具体可验收。
            若教师已填写部分内容，在其基础上优化完善，保留合理表述，不要无故推翻。
            """;

    private final KimiChatService kimiChatService;
    private final ClassroomAccessSupport access;

    public TaskDraftAssistService(KimiChatService kimiChatService, ClassroomAccessSupport access) {
        this.kimiChatService = kimiChatService;
        this.access = access;
    }

    public Map<String, Object> assist(TaskDraftAssistRequest request) {
        access.requireTeacher();
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw new IllegalArgumentException("请描述你想布置的任务（例如课程、周次、主题与子任务数量）");
        }

        String userPrompt = buildUserPrompt(request);
        String raw;
        try {
            raw = kimiChatService.chatWithSystem(SYSTEM_PROMPT, userPrompt, 0.35)
                    .block(Duration.ofSeconds(120));
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Timeout")) {
                throw new IllegalStateException("AI 响应超时（海外服务器访问百炼较慢），请稍后重试或换更短描述");
            }
            throw e;
        }
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("AI 未返回结果，请稍后重试");
        }
        if (raw.startsWith("解析响应出错") || raw.contains("请稍后重试")) {
            throw new IllegalStateException(raw);
        }

        Map<String, Object> parsed = parseDraftJson(raw);
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(parsed);
        result.put("modelNote", "草稿仅供参考，填入表单后请教师确认再发布。");
        return result;
    }

    private String buildUserPrompt(TaskDraftAssistRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【教师需求】\n").append(request.getPrompt().trim()).append('\n');

        boolean hasForm = hasText(request.getTitle()) || hasText(request.getDescription()) || hasText(request.getSubTaskText());
        if (hasForm) {
            sb.append("\n【当前表单草稿】\n");
            if (hasText(request.getTitle())) {
                sb.append("标题：").append(request.getTitle().trim()).append('\n');
            }
            if (hasText(request.getDescription())) {
                sb.append("说明：").append(request.getDescription().trim()).append('\n');
            }
            if (hasText(request.getSubTaskText())) {
                sb.append("子任务（每行一项）：\n").append(request.getSubTaskText().trim()).append('\n');
            }
            sb.append("请在以上基础上优化，使任务更完整、可执行。\n");
        }
        return sb.toString();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private Map<String, Object> parseDraftJson(String raw) {
        try {
            JSONObject json = extractJsonObject(raw);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", json.optString("title", "").trim());
            map.put("description", json.optString("description", "").trim());
            map.put("designNote", json.optString("designNote", "").trim());
            map.put("subTasks", parseSubTasks(json.optJSONArray("subTasks")));
            if (map.get("title") == null || ((String) map.get("title")).isBlank()) {
                throw new IllegalArgumentException("empty title");
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> subs = (List<Map<String, String>>) map.get("subTasks");
            if (subs.isEmpty()) {
                throw new IllegalArgumentException("empty subTasks");
            }
            return map;
        } catch (Exception e) {
            log.warn("任务草稿 JSON 解析失败: {}", e.getMessage());
            throw new IllegalStateException("AI 返回格式异常，请调整描述后重试");
        }
    }

    private static List<Map<String, String>> parseSubTasks(JSONArray arr) {
        List<Map<String, String>> list = new ArrayList<>();
        if (arr == null) {
            return list;
        }
        for (int i = 0; i < arr.length(); i++) {
            Object item = arr.get(i);
            if (item instanceof JSONObject obj) {
                String title = obj.optString("title", "").trim();
                if (title.isEmpty()) {
                    continue;
                }
                Map<String, String> sub = new LinkedHashMap<>();
                sub.put("title", title);
                String desc = obj.optString("description", "").trim();
                if (desc.isEmpty()) {
                    desc = "请提交「" + title + "」成果物。";
                }
                sub.put("description", desc);
                list.add(sub);
            } else if (item instanceof String s && !s.isBlank()) {
                Map<String, String> sub = new LinkedHashMap<>();
                sub.put("title", s.trim());
                sub.put("description", "请提交「" + s.trim() + "」成果物。");
                list.add(sub);
            }
        }
        return list;
    }

    private static JSONObject extractJsonObject(String raw) {
        String s = raw.trim();
        if (s.contains("```")) {
            int jsonFence = s.indexOf("```json");
            int fence = jsonFence >= 0 ? jsonFence : s.indexOf("```");
            int start = s.indexOf('\n', fence);
            if (start < 0) {
                start = fence + 3;
            } else {
                start++;
            }
            int end = s.indexOf("```", start);
            if (end > start) {
                s = s.substring(start, end).trim();
            }
        }
        int brace = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (brace >= 0 && last > brace) {
            s = s.substring(brace, last + 1);
        }
        return new JSONObject(s);
    }
}
