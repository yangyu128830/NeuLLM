package com.neusoft.edu.neullmdev.service.agent.support;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProcessJSONStringTools {

    // 匹配 }, 或 ],，用于修复 LLM 生成的畸形 JSON 中的尾逗号
    private static final Pattern TRAILING_COMMA = Pattern.compile(",(\\s*[\\]\\}])");

    /**
     * 解析 LLM 响应中的函数调用 JSON。
     * 期望格式：{"function": "xxx", "params": {...}}
     * 若响应为自然语言（非 JSON），返回 null。
     */
    public FunctionCall parseFunctionCall(String response) {
        try {
            String cleaned = cleanResponse(response);
            JSONObject json = new JSONObject(cleaned);
            if (json.has("function") && json.has("params")) {
                FunctionCall functionCall = new FunctionCall();
                functionCall.setName(json.getString("function").trim());
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) json.getJSONObject("params").toMap();
                functionCall.setParams(params);
                return functionCall;
            }
            return null;
        } catch (Exception e) {
            // 自然语言回复通常不以 { 开头，返回 null 属正常，避免刷屏
            String head = response == null ? "" : response.strip();
            if (head.startsWith("{") || head.startsWith("```json")) {
                log.warn("JSON 解析失败: {}，原始响应: {}", e.getMessage(), response);
            }
            return null;
        }
    }

    /**
     * 清理 LLM 响应：提取 JSON 代码块或裸 JSON，修复尾逗号和未闭合花括号。
     */
    private String cleanResponse(String response) {
        final String JSON_START = "```json";
        final String JSON_END = "```";

        int startIndex = response.indexOf(JSON_START);
        int endIndex = response.lastIndexOf(JSON_END);

        String jsonContent;
        if (startIndex >= 0 && endIndex > startIndex) {
            // 提取代码块内的 JSON 内容
            jsonContent = response.substring(startIndex + JSON_START.length(), endIndex).trim();
        } else {
            int jsonStart = response.indexOf('{');
            if (jsonStart >= 0) {
                jsonContent = response.substring(jsonStart);
            } else {
                return response;
            }
        }

        // 修复畸形 JSON：去掉尾部逗号（}, 或 ],）
        jsonContent = TRAILING_COMMA.matcher(jsonContent).replaceAll("$1");

        // 修复畸形 JSON：用花括号平衡计数补上缺失的 }，去掉多余的 }
        jsonContent = fixUnclosedBraces(jsonContent);

        return jsonContent;
    }

    /**
     * 花括号平衡修复：LLM 常生成缺少或多出 } 的 JSON。
     * <p>
     * 策略：
     * 1. 统计 { 比 } 多多少（末尾补 }）
     * 2. 遇到多余 }（depth==0 时）直接删除
     * <p>
     * 同时正确追踪"是否在字符串内"，避免把 JSON 字符串里的 { } 算进去。
     */
    private String fixUnclosedBraces(String json) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            // 处理转义字符（如 \"）
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                sb.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                sb.append(c);
                continue;
            }

            // 非字符串内：处理花括号
            if (!inString) {
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    if (depth == 0) {
                        // 多余的 }，直接丢弃
                        continue;
                    }
                    depth--;
                }
            }

            sb.append(c);
        }

        // 末尾补充未闭合的 {
        if (depth > 0) {
            sb.append("}".repeat(depth));
        }

        return sb.toString();
    }
}
