package com.neusoft.edu.neullmdev.dto.classroom;

import lombok.Data;

@Data
public class SendStudentReminderRequest {
    private Long studentUserId;
    /** 可选：指定子任务；为空则对该学生所有待交项催交 */
    private String subTaskId;
}
