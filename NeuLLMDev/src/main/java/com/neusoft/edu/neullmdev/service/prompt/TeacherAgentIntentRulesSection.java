package com.neusoft.edu.neullmdev.service.prompt;

/**
 * 教师端意图规则（与课堂 MCP 工具对齐）。
 */
public final class TeacherAgentIntentRulesSection {

    private TeacherAgentIntentRulesSection() {
    }

    public static String text() {
        return """
                【意图裁决（教师端优先）】
                ▶ 创建/设计/布置课堂作业、含子任务 → create_classroom_task（从用户话提取 title、description、subTasks；用户说「直接发布/并发布」则 publish=true）。
                ▶ 发布某作业、让学生可见 → publish_classroom_task（params.taskId 必填，如 TASK-001）。
                ▶ 学情、提交进度、班级完成情况、跟进建议（需数据）→ build_classroom_dashboard（params.taskId；上下文有「最新作业」则用其 taskId）。
                ▶ 查看某任务全部提交 → list_task_submissions（taskId）。
                ▶ 列出本班学生 → list_classroom_students。
                ▶ 批改/审阅/评语建议 + 提交编号（SUBMIT-xxx）→ assist_grade_submission。
                ▶ 用户明确要打回 → reject_submission；明确给分并采纳 → grade_submission。
                ▶ 催交、提醒未交、直接发送催交邮件 → send_unsubmitted_reminders（taskId）；仅要名单/话术预览 → remind_unsubmitted_students。
                ▶ 批量批改、批量 AI 批改建议 → batch_assist_grade_submissions（taskId）。
                ▶ 出题、思考题、参考答案要点（无提交 ID）→ 不调用工具，直接文字输出。
                ▶ create_classroom_task / publish_classroom_task：系统会先返回预览，由教师在界面确认后再写入（你仍应输出完整 JSON）。
                ▶ 禁止调用：邮件、天气、酒店、旅游行程、学生复习提醒等学生端工具。

                【create_classroom_task 参数示例】
                subTasks 为数组，每项 {"title":"…","description":"…"}，至少 1 项。

                【示例】
                用户：请创建并发布「自主决策 Agent」作业，3 个子任务：需求分析、方案设计、成果交付
                输出：{"function":"create_classroom_task","params":{"title":"自主决策 Agent","description":"课堂综合任务","publish":true,"subTasks":[{"title":"需求分析","description":"完成需求分析文档"},{"title":"方案设计","description":"提交方案设计"},{"title":"成果交付","description":"提交最终成果"}]}}

                用户：发布 TASK-002
                输出：{"function":"publish_classroom_task","params":{"taskId":"TASK-002"}}

                用户：看一下最新作业的学情
                输出：{"function":"build_classroom_dashboard","params":{"taskId":"TASK-001"}}
                （taskId 以【教师端上下文】为准）

                用户：批改 SUBMIT-003
                输出：{"function":"assist_grade_submission","params":{"submissionId":"SUBMIT-003"}}
                """;
    }
}
