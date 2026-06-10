package com.neusoft.edu.neullmdev.service.schedule;

import com.neusoft.edu.neullmdev.dto.schedule.ScheduleParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    public ToolResult addSchedule(ScheduleParams params) {
        JSONObject json = new JSONObject();
        json.put("functionName", "add_schedule");
        json.put("ok", false);
        json.put("message", "日程工具尚未实现");
        if (params != null && params.getTitle() != null) {
            json.put("title", params.getTitle());
        }
        return McpToolSupport.fromJsonString("add_schedule", json.toString());
    }
}
