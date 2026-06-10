package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 「月日 + 计划去某地」且无完整攻略诉求时，只应查天气锦囊；若模型误输出 plan_itinerary，改回 get_current_weather。
 */
@Component
public class TravelPrepWeatherOverride {

    private static final Pattern MONTH_DAY = Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*(?:日|号)?");
    private static final Pattern DEST_PLAN = Pattern.compile("(?:计划去|要去|准备去|打算去)\\s*([\\u4e00-\\u9fa5]{2,12}(?:市|县|区)?)");

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        if (userInput == null || userInput.isBlank() || parsed == null || parsed.getName() == null) {
            return parsed;
        }
        if (isLearningPlanningIntent(userInput)) {
            return parsed;
        }
        if (!"plan_itinerary".equalsIgnoreCase(parsed.getName().trim())) {
            return parsed;
        }
        if (!isDatedTripPrep(userInput) || wantsFullItinerary(userInput)) {
            return parsed;
        }
        String loc = extractLocation(parsed, userInput);
        if (loc == null || loc.isBlank()) {
            return parsed;
        }
        FunctionCall fc = new FunctionCall();
        fc.setName("get_current_weather");
        Map<String, Object> p = new HashMap<>();
        p.put("location", loc.replaceAll("(市|县|区)$", ""));
        fc.setParams(p);
        return fc;
    }

    private boolean isLearningPlanningIntent(String s) {
        if (s.contains("复习计划") || s.contains("学习计划") || s.contains("备考计划")) {
            return true;
        }
        if (s.contains("复习") && (s.contains("规划") || s.contains("安排"))) {
            return true;
        }
        if ((s.contains("学习") || s.contains("课程") || s.contains("考试") || s.contains("知识点") || s.contains("错题"))
                && (s.contains("计划") || s.contains("规划"))) {
            return true;
        }
        return false;
    }

    private boolean isDatedTripPrep(String s) {
        if (!MONTH_DAY.matcher(s).find()) {
            return false;
        }
        if (!(s.contains("计划去") || s.contains("要去") || s.contains("准备去") || s.contains("打算去"))) {
            return false;
        }
        if ((s.contains("发邮件") || s.contains("发送邮件")) && s.contains("@")) {
            return false;
        }
        return true;
    }

    private boolean wantsFullItinerary(String s) {
        if (s.contains("生成规划") || s.contains("给我规划") || s.contains("规划一下") || s.contains("行程规划")
                || s.contains("行程安排") || s.contains("旅行规划") || s.contains("游玩攻略") || s.contains("旅游攻略")
                || s.contains("怎么玩") || s.contains("安排行程") || s.contains("做个攻略") || s.contains("攻略")) {
            return true;
        }
        return s.contains("规划") && (s.matches(".*\\d+\\s*天.*") || s.contains("预算"));
    }

    private String extractLocation(FunctionCall parsed, String userInput) {
        if (parsed.getParams() != null) {
            Object d = parsed.getParams().get("destination");
            if (d != null && !String.valueOf(d).isBlank()) {
                return String.valueOf(d).trim();
            }
        }
        Matcher m = DEST_PLAN.matcher(userInput);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }
}
