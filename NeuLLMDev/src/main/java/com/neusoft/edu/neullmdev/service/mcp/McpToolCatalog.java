package com.neusoft.edu.neullmdev.service.mcp;

import com.neusoft.edu.neullmdev.model.mcp.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP 工具元数据唯一来源：协议 tools/list 与 Agent 系统提示中的工具列表均由此生成。
 */
@Component
public class McpToolCatalog {

    private static final List<ToolDefinition> DEFINITIONS = List.of(
            def("send_email", "发送邮件。recipient 必填；subject、content 可选。",
                    "send_email(recipient, subject, content)",
                    Map.of("recipient", str("收件人邮箱"), "subject", str("邮件主题"), "content", str("邮件正文")),
                    List.of("recipient")),
            def("create_travel_reminder", "创建旅行/学习提醒。",
                    "create_travel_reminder(title, location, datetime, notes, advanceNotice)",
                    Map.of("title", str("提醒标题"), "location", str("地点，可空"),
                            "datetime", str("提醒时间"), "notes", str("备注"), "advanceNotice", str("提前分钟")),
                    List.of("title", "datetime")),
            def("plan_itinerary", "生成多日城市旅游行程攻略。",
                    "plan_itinerary(destination, days, budget, interests)",
                    Map.of("destination", str("目的地"), "days", num("天数"), "budget", num("预算"), "interests", str("偏好")),
                    List.of("destination", "days")),
            def("hotel_recommend", "推荐酒店。",
                    "hotel_recommend(city, origin, checkInDate, checkOutDate, guests, priceRange, keywords)",
                    Map.of("city", str("城市")), List.of("city")),
            def("food_search", "搜索美食。",
                    "food_search(keyword, region, adcode, latitude, longitude, radius, page_size, distance_order)",
                    Map.of("keyword", str("关键词")), List.of("keyword")),
            def("recommend_destination", "推荐目的地。",
                    "recommend_destination(preferences, budget, days)",
                    Map.of("preferences", str("偏好")), List.of()),
            def("hotel_book", "预订酒店。",
                    "hotel_book(hotelName, checkInDate, checkOutDate, guests, contactName, contactPhone, contactEmail, specialRequests)",
                    Map.of("hotelName", str("酒店名")), List.of("hotelName")),
            def("get_current_weather", "查询天气。",
                    "get_current_weather(location)",
                    Map.of("location", str("城市")), List.of("location")),
            def("save_user_profile", "保存用户资料。",
                    "save_user_profile(external_id, display_name, real_name, email, phone, address, gender, avatar_url, remark)",
                    Map.of("email", str("邮箱")), List.of()),
            def("get_email_info", "按别名查 emailaddress 表。",
                    "get_email_info(account)",
                    Map.of("account", str("联系人别名")), List.of("account")),
            def("word_write", "生成 Word。",
                    "word_write(title, content)",
                    Map.of("title", str("标题"), "content", str("正文")), List.of("title", "content")),
            def("get_location_info", "查询地点/路线。",
                    "get_location_info(location, origin, destination)",
                    Map.of("location", str("地点")), List.of("location")),
            def("send_sms", "发送短信。",
                    "send_sms(phoneNumber, message)",
                    Map.of("phoneNumber", str("手机号"), "message", str("内容")),
                    List.of("phoneNumber", "message")),
            def("add_schedule", "添加日程（演示）。",
                    "add_schedule()",
                    Map.of(), List.of()),
            def("list_classroom_students", "教师：列出本班学生。",
                    "list_classroom_students(classId)",
                    Map.of("classId", str("班级ID，可空")), List.of()),
            def("create_classroom_task", "教师：创建课堂作业（含子任务）。",
                    "create_classroom_task(title, description, publish, subTasks)",
                    Map.of("title", str("标题"), "description", str("说明"), "publish", str("是否发布 true/false"),
                            "subTasks", Map.of("type", "array", "description", "子任务列表")),
                    List.of("title")),
            def("publish_classroom_task", "教师：发布作业。",
                    "publish_classroom_task(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("build_classroom_dashboard", "教师：生成作业提交进度看板。",
                    "build_classroom_dashboard(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("list_task_submissions", "教师：查看任务全部提交。",
                    "list_task_submissions(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("assist_grade_submission", "教师：AI 总结学生提交并给出批改建议（摘要、建议分数与评语、是否建议打回）。",
                    "assist_grade_submission(submissionId)",
                    Map.of("submissionId", str("提交ID，如 SUBMIT-001")),
                    List.of("submissionId")),
            def("grade_submission", "教师：批改打分。",
                    "grade_submission(submissionId, score, comment)",
                    Map.of("submissionId", str("提交ID"), "score", num("分数"), "comment", str("评语")),
                    List.of("submissionId", "score")),
            def("reject_submission", "教师：打回重交。",
                    "reject_submission(submissionId, comment)",
                    Map.of("submissionId", str("提交ID"), "comment", str("打回原因")),
                    List.of("submissionId", "comment")),
            def("remind_unsubmitted_students", "教师：生成未交/需重交学生名单与催交话术（预览）。",
                    "remind_unsubmitted_students(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("send_unsubmitted_reminders", "教师：向未交/需重交学生直接发送催交邮件（需 user_profile 中有邮箱）。",
                    "send_unsubmitted_reminders(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("batch_assist_grade_submissions", "教师：对待批改提交批量生成 AI 批改建议（最多 8 条）。",
                    "batch_assist_grade_submissions(taskId)",
                    Map.of("taskId", str("任务ID")), List.of("taskId")),
            def("list_my_assignments", "学生：查看已发布作业。",
                    "list_my_assignments()",
                    Map.of(), List.of()),
            def("submit_assignment", "学生：文本提交作业（文件请用页面上传）。",
                    "submit_assignment(taskId, subTaskId, fileName, content)",
                    Map.of("taskId", str("任务ID"), "subTaskId", str("子任务ID"), "fileName", str("文件名"),
                            "content", str("正文")),
                    List.of("taskId", "subTaskId", "content"))
    );

    private static final Set<String> TEACHER_TOOL_NAMES = Set.of(
            "list_classroom_students",
            "create_classroom_task",
            "publish_classroom_task",
            "build_classroom_dashboard",
            "list_task_submissions",
            "assist_grade_submission",
            "grade_submission",
            "reject_submission",
            "remind_unsubmitted_students",
            "send_unsubmitted_reminders",
            "batch_assist_grade_submissions"
    );

    public List<McpTool> listTools() {
        return DEFINITIONS.stream().map(ToolDefinition::toMcpTool).toList();
    }

    /** 教师端 Agent 仅暴露课堂工具，减少误选学生/出行类工具。 */
    public String renderTeacherPromptToolList() {
        StringBuilder sb = new StringBuilder("【课堂工具列表】\n");
        int i = 1;
        for (ToolDefinition t : DEFINITIONS) {
            if (!TEACHER_TOOL_NAMES.contains(t.name())) {
                continue;
            }
            sb.append(i++).append(". ").append(t.paramSignature())
                    .append(" — ").append(t.description()).append('\n');
        }
        return sb.toString().trim();
    }

    /** 供 LLM 系统提示使用的编号工具列表（与 {@link #listTools()} 工具名一致）。 */
    public String renderPromptToolList() {
        StringBuilder sb = new StringBuilder("【工具列表】\n");
        for (int i = 0; i < DEFINITIONS.size(); i++) {
            ToolDefinition t = DEFINITIONS.get(i);
            sb.append(i + 1).append(". ").append(t.paramSignature())
                    .append(" — ").append(t.description()).append('\n');
        }
        return sb.toString().trim();
    }

    private static ToolDefinition def(String name, String description, String paramSignature,
                                      Map<String, Map<String, Object>> properties, List<String> required) {
        return new ToolDefinition(name, description, paramSignature, properties, required);
    }

    private static Map<String, Object> str(String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> num(String description) {
        return Map.of("type", "number", "description", description);
    }

    private record ToolDefinition(
            String name,
            String description,
            String paramSignature,
            Map<String, Map<String, Object>> properties,
            List<String> required) {

        McpTool toMcpTool() {
            return new McpTool(name, description, Map.of(
                    "type", "object",
                    "properties", properties,
                    "required", required
            ));
        }
    }
}
