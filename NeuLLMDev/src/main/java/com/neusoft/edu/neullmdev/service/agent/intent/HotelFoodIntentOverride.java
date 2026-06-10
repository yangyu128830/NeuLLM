package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 避免小模型用腾讯 food_search（keyword=酒店）搜住宿；住宿应走高德 hotel_recommend。
 */
@Component
public class HotelFoodIntentOverride {

    private static final Pattern CITY_BEFORE_HOTEL = Pattern.compile(
            "(?:找|搜|查|推荐|订|要)\\s*([\\u4e00-\\u9fa5]{2,10})\\s*(?:市|县|区)?\\s*(?:有|一下|几家)?\\s*的?\\s*(?:酒店|宾馆|旅馆|民宿)");

    private static final Pattern HOTEL_IN_CITY = Pattern.compile(
            "([\\u4e00-\\u9fa5]{2,10}?)\\s*(?:市|县|区)?\\s*(?:有|的)?\\s*(?:什么)?\\s*(?:好)?\\s*(?:的)?\\s*(?:酒店|宾馆|旅馆|民宿)");

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        if (userInput == null || userInput.isBlank() || parsed == null) {
            return parsed;
        }
        String name = parsed.getName() == null ? "" : parsed.getName().toLowerCase().trim();
        if (!"food_search".equals(name)) {
            return parsed;
        }
        if (!shouldRouteToHotel(userInput, parsed)) {
            return parsed;
        }
        return buildHotelRecommend(userInput, parsed);
    }

    private boolean shouldRouteToHotel(String input, FunctionCall foodCall) {
        Map<String, Object> p = foodCall.getParams();
        String keyword = "";
        if (p != null && p.get("keyword") != null) {
            keyword = String.valueOf(p.get("keyword")).trim();
        }
        if (isLodgingKeyword(keyword)) {
            return true;
        }
        // 原文明确找「住」；若同时出现强餐饮词（如酒店旁吃火锅）则不要误判
        if (input.matches(".*(火锅|日料|烧烤|小吃|美食城|吃点|吃啥|餐厅|饭馆|订餐).*")) {
            return false;
        }
        if (input.matches(".*(找|搜|查|推荐|订).{0,18}(酒店|宾馆|旅馆|民宿|住宿).*")) {
            return true;
        }
        if (input.contains("住哪") || input.contains("住店") || input.contains("订房")) {
            return true;
        }
        return false;
    }

    private boolean isLodgingKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        if (keyword.matches("(酒店|宾馆|旅馆|住宿|民宿|客栈|旅店)")) {
            return true;
        }
        // 「海景酒店」类仍属住宿检索
        if (keyword.endsWith("酒店") && !keyword.contains("餐") && !keyword.contains("吃")
                && !keyword.contains("火锅") && !keyword.contains("自助餐厅")) {
            return true;
        }
        return false;
    }

    private FunctionCall buildHotelRecommend(String userInput, FunctionCall foodCall) {
        String city = "";
        Map<String, Object> p = foodCall.getParams();
        if (p != null) {
            if (p.get("region") != null) {
                city = String.valueOf(p.get("region")).trim();
            }
        }
        if (city.isEmpty()) {
            city = extractCity(userInput);
        }

        FunctionCall fc = new FunctionCall();
        fc.setName("hotel_recommend");
        Map<String, Object> hp = new HashMap<>();
        hp.put("city", city);
        hp.put("origin", "");
        hp.put("checkInDate", "");
        hp.put("checkOutDate", "");
        hp.put("guests", 2);
        hp.put("priceRange", "");
        String kw = "";
        if (p != null && p.get("keyword") != null) {
            kw = String.valueOf(p.get("keyword")).trim();
        }
        // 住宿偏好关键词（海景酒店等）带入 keywords，纯「酒店」则空
        if (!kw.isEmpty() && !kw.matches("(酒店|宾馆|旅馆|住宿|民宿|客栈|旅店)")) {
            hp.put("keywords", kw);
        } else {
            hp.put("keywords", "");
        }
        fc.setParams(hp);
        return fc;
    }

    private String extractCity(String input) {
        Matcher m1 = CITY_BEFORE_HOTEL.matcher(input);
        if (m1.find()) {
            return cleanCity(m1.group(1));
        }
        Matcher m2 = HOTEL_IN_CITY.matcher(input);
        if (m2.find()) {
            return cleanCity(m2.group(1));
        }
        return "";
    }

    private static String cleanCity(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("市", "").replace("县", "").replace("区", "").trim();
    }
}
