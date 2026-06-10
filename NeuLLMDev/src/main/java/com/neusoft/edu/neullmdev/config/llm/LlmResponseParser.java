package com.neusoft.edu.neullmdev.config.llm;

import org.json.JSONArray;
import org.json.JSONObject;

public final class LlmResponseParser {

    private LlmResponseParser() {}

    /**
     * 解析 Chat Completions 响应中的助手正文：{@code choices[0].message.content}（OpenAI 兼容格式，适用百炼/Kimi 等）。
     */
    public static String assistantContent(String raw) {
        try {
            JSONObject jsonResp = new JSONObject(raw);
            if (jsonResp.has("choices")) {
                JSONArray choices = jsonResp.optJSONArray("choices");
                if (choices != null && choices.length() > 0) {
                    JSONObject first = choices.optJSONObject(0);
                    if (first != null) {
                        JSONObject message = first.optJSONObject("message");
                        if (message != null) {
                            return message.optString("content", "");
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
