package com.neusoft.edu.neullmdev.model.agent;

import lombok.Data;

import java.util.Map;

/** Agent 解析 LLM 输出的函数调用。 */
@Data
public class FunctionCall {
    private String name;
    private Map<String, Object> params;
}
