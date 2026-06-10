package com.neusoft.edu.neullmdev.service.travel;

import com.neusoft.edu.neullmdev.dto.travel.RecommendDestinationParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendDestinationService {

    public ToolResult recommend(RecommendDestinationParams params) {
        return McpToolSupport.fromJsonString("recommend_destination",
                recommendDestination("recommend_destination", params));
    }

    public String recommendDestination(String functionName, RecommendDestinationParams params) {
        String preferences = params == null || params.getPreferences() == null ? "" : params.getPreferences().trim();
        String budget = params == null || params.getBudget() == null ? "" : params.getBudget().trim();
        int days = params == null || params.getDays() == null ? 3 : Math.max(1, params.getDays());

        String p = preferences.toLowerCase();
        int budgetAmount = parseBudgetAmount(budget);
        String budgetLevel = inferBudgetLevel(budget, budgetAmount);
        String referenceCity = extractReferenceCity(preferences);

        List<DestinationCandidate> pool = buildRegionalPool(referenceCity);
        List<DestinationCandidate> matched = new ArrayList<>();

        for (DestinationCandidate item : pool) {
            if (!isBudgetMatch(item.budgetLevel, budgetLevel)) {
                continue;
            }
            if (!isDayMatch(item.suggestedDays, days)) {
                continue;
            }
            if (!isPreferenceMatch(item.tags, p)) {
                continue;
            }
            matched.add(item);
        }

        if (matched.isEmpty()) {
            for (DestinationCandidate item : pool) {
                if (isBudgetMatch(item.budgetLevel, budgetLevel) && isDayMatch(item.suggestedDays, days)) {
                    matched.add(item);
                }
            }
        }

        if (matched.isEmpty()) {
            matched.addAll(defaultNationalPool());
        }

        JSONArray recommendations = new JSONArray();
        int limit = Math.min(3, matched.size());
        for (int i = 0; i < limit; i++) {
            DestinationCandidate item = matched.get(i);
            recommendations.put(buildRecommendation(item.city, item.reason, item.plan, item.budgetLevel));
        }

        JSONObject result = new JSONObject();
        result.put("functionName", functionName);
        result.put("preferences", preferences);
        result.put("budget", budget);
        result.put("days", days);
        result.put("referenceCity", safe(referenceCity));
        result.put("summary", String.format("已结合%s附近、预算%s、行程%d天和兴趣偏好，为你推荐以下目的地。",
                safe(referenceCity), safe(budget), days));
        result.put("recommendations", recommendations);
        return result.toString();
    }

    private List<DestinationCandidate> buildRegionalPool(String referenceCity) {
        String city = referenceCity == null ? "" : referenceCity;
        List<DestinationCandidate> list = new ArrayList<>();
        if (city.contains("大连") || city.contains("沈阳") || city.contains("哈尔滨") || city.contains("长春")) {
            list.add(new DestinationCandidate("青岛", "海滨体验好，和东北出发交通便利，适合短假期。", "栈桥+八大关+崂山", "中", 3, "海", "城市", "美食"));
            list.add(new DestinationCandidate("烟台", "海岸线风景舒适，节奏慢，适合放松。", "养马岛+蓬莱阁+海昌渔人码头", "中", 3, "海", "休闲"));
            list.add(new DestinationCandidate("威海", "海景干净，适合轻度度假。", "刘公岛+国际海水浴场+火炬八街", "中", 2, "海", "休闲"));
            list.add(new DestinationCandidate("长白山", "自然景观突出，适合徒步和亲近自然。", "天池+北坡森林步道", "中高", 3, "自然", "徒步", "山"));
        } else if (city.contains("北京") || city.contains("天津") || city.contains("石家庄")) {
            list.add(new DestinationCandidate("济南", "高铁便捷，泉城文化与美食兼具。", "趵突泉+大明湖+曲水亭街", "中", 2, "文化", "美食", "城市"));
            list.add(new DestinationCandidate("西安", "历史文化深度游性价比高。", "兵马俑+城墙+大唐不夜城", "中", 3, "历史", "文化"));
            list.add(new DestinationCandidate("青岛", "海滨+城市组合，适合周末或小长假。", "栈桥+八大关+崂山", "中", 3, "海", "城市"));
        } else {
            list.addAll(defaultNationalPool());
        }
        return list;
    }

    private List<DestinationCandidate> defaultNationalPool() {
        List<DestinationCandidate> list = new ArrayList<>();
        list.add(new DestinationCandidate("杭州", "景观友好，适合短途放松。", "西湖+灵隐寺+龙井村", "中", 3, "自然", "城市", "休闲"));
        list.add(new DestinationCandidate("成都", "综合体验均衡，美食与城市休闲兼具。", "宽窄巷子+都江堰+熊猫基地", "中", 3, "美食", "城市"));
        list.add(new DestinationCandidate("厦门", "海景与城市体验兼顾，节奏舒适。", "鼓浪屿+环岛路+沙坡尾", "中", 3, "海", "休闲"));
        list.add(new DestinationCandidate("西安", "历史文化资源集中，美食丰富。", "兵马俑+城墙+大唐不夜城", "中", 3, "历史", "文化"));
        return list;
    }

    private JSONObject buildRecommendation(String city, String reason, String plan, String budgetLevel) {
        JSONObject obj = new JSONObject();
        obj.put("destination", city);
        obj.put("reason", reason);
        obj.put("suggestedPlan", plan);
        obj.put("budgetLevel", budgetLevel);
        return obj;
    }

    private boolean isPreferenceMatch(String[] tags, String p) {
        if (p == null || p.isBlank()) {
            return true;
        }
        for (String tag : tags) {
            if (tag != null && !tag.isBlank() && p.contains(tag)) {
                return true;
            }
        }
        return !(p.contains("海") || p.contains("历史") || p.contains("文化") || p.contains("自然") || p.contains("徒步") || p.contains("山"));
    }

    private boolean isBudgetMatch(String candidateBudget, String userBudget) {
        if (userBudget == null || userBudget.isBlank()) {
            return true;
        }
        if ("高".equals(userBudget)) {
            return "中高".equals(candidateBudget) || "高".equals(candidateBudget) || "中".equals(candidateBudget);
        }
        if ("低".equals(userBudget)) {
            return "低".equals(candidateBudget) || "低中".equals(candidateBudget) || "中".equals(candidateBudget);
        }
        return true;
    }

    private boolean isDayMatch(int suggestedDays, int userDays) {
        return Math.abs(suggestedDays - userDays) <= 1;
    }

    private int parseBudgetAmount(String budget) {
        if (budget == null) {
            return -1;
        }
        String digits = budget.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String inferBudgetLevel(String budgetText, int amount) {
        String text = budgetText == null ? "" : budgetText;
        if (text.contains("高") || amount >= 6000) {
            return "高";
        }
        if (text.contains("低") || text.contains("省") || (amount > 0 && amount <= 2500)) {
            return "低";
        }
        if (amount > 0 && amount <= 5000) {
            return "中";
        }
        return "中";
    }

    private String extractReferenceCity(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] cityKeywords = {"大连", "沈阳", "哈尔滨", "长春", "北京", "天津", "石家庄", "上海", "杭州", "成都", "重庆", "广州", "深圳"};
        for (String city : cityKeywords) {
            if (text.contains(city)) {
                return city;
            }
        }
        return "";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未指定" : value;
    }

    private static class DestinationCandidate {
        private final String city;
        private final String reason;
        private final String plan;
        private final String budgetLevel;
        private final int suggestedDays;
        private final String[] tags;

        private DestinationCandidate(String city, String reason, String plan, String budgetLevel, int suggestedDays, String... tags) {
            this.city = city;
            this.reason = reason;
            this.plan = plan;
            this.budgetLevel = budgetLevel;
            this.suggestedDays = suggestedDays;
            this.tags = tags;
        }
    }
}
