package com.neusoft.edu.neullmdev.service.prompt;

/**
 * Agent 意图裁决规则与 JSON 输出示例（与 {@link com.neusoft.edu.neullmdev.service.mcp.McpToolCatalog} 工具名对齐）。
 */
public final class AgentIntentRulesSection {

    private AgentIntentRulesSection() {
    }

    public static String text() {
        return """
                【意图裁决（优先顺序）】
                ▶ 用户给出收件邮箱（含@）且描述发邮件、主题、正文 → 只能 send_email。「主题是学习提醒」只是邮件标题文字。
                ▶ 用户要把自己的姓名、邮箱、电话、地址等信息保存到「用户信息 / user_profile / 数据库」→ save_user_profile（从用户话里提取字段作预填；真正写入由用户在界面卡片确认后完成）。
                ▶ 用户说「提醒我 / 设提醒 / 定时提醒」且未涉及发邮件、邮箱 → create_travel_reminder。
                ▶ 整理知识点、讲解概念、复习提纲、制定「课程/科目」学习计划与时间安排 → 不要调用工具，直接文字回答（可分点、按天）。
                ▶ 用户明确查天气 → get_current_weather。
                ▶ 用户要旅游城市多日游玩攻略、景点行程安排、出游预算行程 → plan_itinerary（目的地为真实城市）。
                ▶ 用户明确酒店住宿 → hotel_recommend；明确餐饮美食 → food_search；明确预订酒店并给联系人 → hotel_book。
                ▶ 仅有意图而无目的地 → recommend_destination。
                ▶ 教师总结/审阅/批改建议某次学生提交（含 submissionId 或「这份作业」）→ assist_grade_submission；确认采纳后再 grade_submission 或 reject_submission。

                【plan_itinerary 补充】
                禁止：课程复习计划、期末备考周计划、数据结构学习计划 → 请直接文字回答，勿调用 plan_itinerary。

                【create_travel_reminder 补充】
                别名：模型若习惯输出 setTravelReminder，后端会识别为 create_travel_reminder。

                【示例】
                用户：整理 JavaSE 核心知识点
                输出：（直接文字，分点整理）

                用户：Java 多线程怎么理解？
                输出：（直接文字，通俗解释）

                用户：制定一周数据结构复习计划，每天 2 小时
                输出：（直接文字，按天安排）

                用户：发邮件到 a@b.com，主题是今晚复习，内容是 20:00 刷题
                输出：{"function":"send_email","params":{"recipient":"a@b.com","subject":"今晚复习","content":"20:00 刷题"}}

                用户：今晚 15 点提醒我背单词（或：今天 15.00 闹钟）
                输出：{"function":"create_travel_reminder","params":{"title":"背单词","location":"","datetime":"2026-05-11T15:00","notes":"","advanceNotice":"15"}}
                （datetime 须与用户说的日期、时刻一致；「今天」用当天 yyyy-MM-dd + T + 24 小时制时刻）

                用户：去北京玩三天预算两千，生成游玩攻略
                输出：{"function":"plan_itinerary","params":{"destination":"北京","days":3,"budget":2000,"interests":""}}

                用户：把我信息存进用户表，邮箱 a@b.com，手机 13800138000，我叫王小明
                输出：{"function":"save_user_profile","params":{"real_name":"王小明","email":"a@b.com","phone":"13800138000"}}
                """;
    }
}
