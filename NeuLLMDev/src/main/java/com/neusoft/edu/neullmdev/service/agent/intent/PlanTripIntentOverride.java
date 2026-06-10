package com.neusoft.edu.neullmdev.service.agent.intent;

import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户明确要求「行程/攻略/规划」时，小模型常误调 hotel_recommend。
 * 强制改为 plan_itinerary。
 */
@Component
public class PlanTripIntentOverride {

    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*天");
    private static final Pattern BUDGET = Pattern.compile("预算\\s*(\\d+)");
    private static final Pattern DEST_WO_QU = Pattern.compile("(?:我要去|想去|去)\\s*([^，,。\\s]{2,12}?)(?:[，,。\\s]|玩|预算|$)");
    private static final Pattern DEST_PLAY = Pattern.compile("去\\s*([^，,。\\s]{2,12}?)\\s*玩");
    private static final Pattern DEST_PLAN_GO = Pattern.compile("(?:计划去|要去|准备去|打算去)\\s*([\\u4e00-\\u9fa5]{2,12}(?:市|县|区)?)");

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        if (userInput == null || userInput.isBlank() || parsed == null) {
            return parsed;
        }
        if (isLearningPlanningIntent(userInput)) {
            return parsed;
        }
        String name = parsed.getName() == null ? "" : parsed.getName().toLowerCase().trim();
        if (!hasPlanningIntent(userInput)) {
            return parsed;
        }
        if (!"hotel_recommend".equals(name) && !"food_search".equals(name) && !"recommend_destination".equals(name)) {
            return parsed;
        }
        return buildPlanItinerary(userInput, parsed);
    }

    /** 课程学习类「复习/学习计划」不走旅游城市行程规划工具 */
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

    private boolean hasPlanningIntent(String s) {
        if (s.contains("生成规划") || s.contains("给我规划") || s.contains("规划一下") || s.contains("行程规划")
                || s.contains("行程安排") || s.contains("旅行规划") || s.contains("游玩攻略") || s.contains("旅游攻略")
                || s.contains("怎么玩") || s.contains("安排行程") || s.contains("做个攻略") || s.contains("攻略")) {
            return true;
        }
        if (s.contains("规划") && (s.matches(".*\\d+\\s*天.*") || s.contains("预算"))) {
            return true;
        }
        return false;
    }

    private FunctionCall buildPlanItinerary(String userInput, FunctionCall wrong) {
        String dest = extractDestination(userInput, wrong);
        Integer days = extractDays(userInput);
        Integer budget = extractBudget(userInput);
        if (days == null) {
            days = 3;
        }
        if (budget == null || budget <= 0) {
            budget = 3000;
        }

        FunctionCall fc = new FunctionCall();
        fc.setName("plan_itinerary");
        Map<String, Object> p = new HashMap<>();
        p.put("destination", dest != null && !dest.isBlank() ? dest.trim() : "");
        p.put("days", days);
        p.put("budget", budget);
        p.put("interests", "");
        fc.setParams(p);
        return fc;
    }

    private String extractDestination(String input, FunctionCall wrong) {
        Matcher m = DEST_PLAN_GO.matcher(input);
        if (m.find()) {
            return cleanCity(m.group(1));
        }
        m = DEST_WO_QU.matcher(input);
        if (m.find()) {
            return cleanCity(m.group(1));
        }
        m = DEST_PLAY.matcher(input);
        if (m.find()) {
            return cleanCity(m.group(1));
        }
        if (wrong.getParams() != null && wrong.getParams().get("city") != null) {
            return String.valueOf(wrong.getParams().get("city")).trim();
        }
        return "";
    }

    private static String cleanCity(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("(市|县|区)$", "").trim();
    }

    private Integer extractDays(String input) {
        Matcher m = DAYS.matcher(input);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer extractBudget(String input) {
        Matcher m = BUDGET.matcher(input);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
