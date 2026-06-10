package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小模型常把「XX天气」误判为 food_search（只带 region、缺 keyword），误调腾讯美食接口。
 * 用户明确查天气时，强制走 get_current_weather。
 */
@Component
public class WeatherIntentOverride {

    private static final Pattern CITY_BEFORE_WEATHER = Pattern.compile(
            "(?:今天|明天|后天|请问|请问一下|查|查询|我想知道)?\\s*([\\u4e00-\\u9fa5]{2,8}(?:市|县|区)?)\\s*天气");

    /** 直辖市 / 常见简称，用于兜底提取 */
    private static final Pattern KNOWN_CITY = Pattern.compile(
            "(北京|上海|天津|重庆|深圳|广州|杭州|南京|成都|武汉|西安|大连|青岛|厦门|苏州|沈阳|哈尔滨|长春|济南|郑州|长沙|昆明|福州|合肥|石家庄|太原|南昌|贵阳|海口|兰州|银川|西宁|乌鲁木齐|拉萨|呼和浩特)");

    private static final Set<String> OVERRIDE_FROM = Set.of(
            "food_search",
            "hotel_recommend",
            "plan_itinerary",
            "recommend_destination"
    );

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        if (userInput == null || userInput.isBlank()) {
            return parsed;
        }
        if (!isExplicitWeatherQuery(userInput)) {
            return parsed;
        }
        if (parsed == null || parsed.getName() == null || parsed.getName().isBlank()) {
            return buildWeatherCall(userInput, null);
        }
        String n = parsed.getName().toLowerCase().trim();
        if ("get_current_weather".equals(n)) {
            return parsed;
        }
        if (OVERRIDE_FROM.contains(n)) {
            return buildWeatherCall(userInput, parsed);
        }
        return parsed;
    }

    private boolean isExplicitWeatherQuery(String s) {
        if (s.contains("天气") || s.contains("气温") || s.contains("下雨") || s.contains("下雪")
                || s.contains("降雨") || s.contains("降雪") || s.contains("天气预报")
                || s.contains("风力") || s.contains("风向") || s.contains("雾霾")
                || s.contains("空气质量") || s.contains("AQI") || s.contains("冷不冷")
                || s.contains("热不热") || s.contains("多少度")) {
            // 避免「主题是天气」类邮件误触：有发邮件+邮箱则交给 SendEmailIntentOverride
            if ((s.contains("发邮件") || s.contains("发送邮件")) && s.contains("@")) {
                return false;
            }
            return true;
        }
        return false;
    }

    private FunctionCall buildWeatherCall(String userInput, FunctionCall wrongCall) {
        String loc = extractLocation(userInput, wrongCall);
        if (loc == null || loc.isBlank()) {
            loc = "北京";
        }
        FunctionCall fc = new FunctionCall();
        fc.setName("get_current_weather");
        Map<String, Object> p = new HashMap<>();
        p.put("location", loc.replaceAll("(市|县|区)$", ""));
        fc.setParams(p);
        return fc;
    }

    private String extractLocation(String userInput, FunctionCall wrongCall) {
        Matcher m1 = CITY_BEFORE_WEATHER.matcher(userInput);
        if (m1.find()) {
            return m1.group(1).trim();
        }
        if (wrongCall != null && wrongCall.getParams() != null) {
            Object r = wrongCall.getParams().get("region");
            if (r != null && !String.valueOf(r).isBlank()) {
                return String.valueOf(r).trim();
            }
        }
        Matcher m2 = KNOWN_CITY.matcher(userInput);
        if (m2.find()) {
            return m2.group(1);
        }
        return "";
    }
}
