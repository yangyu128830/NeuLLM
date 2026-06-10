package com.neusoft.edu.neullmdev.dto.classroom;

import lombok.Data;

@Data
public class TaskDraftAssistRequest {
    /** 教师对任务的简要描述或补充要求 */
    private String prompt;
    /** 表单中已有内容（可选，供 AI 在此基础上优化） */
    private String title;
    private String description;
    private String subTaskText;
}
