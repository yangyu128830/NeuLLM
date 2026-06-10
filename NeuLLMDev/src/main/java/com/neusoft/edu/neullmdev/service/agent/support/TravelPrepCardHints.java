package com.neusoft.edu.neullmdev.service.agent.support;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天气 SSE：为「月日 + 计划去某地」类问句生成卡片标题，供前端展示（无地图、无行程）。
 */
public final class TravelPrepCardHints {

    private static final Pattern MONTH_DAY = Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*(?:日|号)?");
    private static final Pattern DEST_PLAN = Pattern.compile("(?:计划去|要去|准备去|打算去)\\s*([\\u4e00-\\u9fa5]{2,12}(?:市|县|区)?)");

    private TravelPrepCardHints() {
    }

    public static JSONObject weatherStartPayload(String userInput) {
        JSONObject o = new JSONObject();
        if (userInput == null || userInput.isBlank()) {
            o.put("cardTitle", "");
            o.put("cardHint", "天气 · 穿衣 · 小贴示");
            return o;
        }
        Matcher md = MONTH_DAY.matcher(userInput);
        Matcher city = DEST_PLAN.matcher(userInput);
        if (md.find() && city.find()) {
            String c = city.group(1).replaceAll("(市|县|区)$", "");
            o.put("cardTitle", c + " · " + md.group(1) + "月" + md.group(2) + "日");
        } else {
            o.put("cardTitle", "");
        }
        o.put("cardHint", "天气 · 穿衣 · 小贴示（精简）");
        return o;
    }
}
