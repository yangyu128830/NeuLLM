package com.neusoft.edu.neullmdev.service.travel;

import com.neusoft.edu.neullmdev.dto.travel.PlanItineraryParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PlanItineraryService {

    private static final String POI_BASE_URL = "https://restapi.amap.com/v3/place/text";
    private static final String GEO_BASE_URL = "https://restapi.amap.com/v3/geocode/geo";

    private final WebClient poiClient;
    private final WebClient geoClient;

    @Value("${amap.api-key:}")
    private String apiKey;

    public PlanItineraryService(WebClient.Builder webClientBuilder) {
        this.poiClient = webClientBuilder.baseUrl(POI_BASE_URL).build();
        this.geoClient = webClientBuilder.baseUrl(GEO_BASE_URL).build();
    }

    public ToolResult plan(PlanItineraryParams params) {
        return McpToolSupport.fromJsonString("plan_itinerary", planItinerary("plan_itinerary", params));
    }

    public String planItinerary(String functionName, PlanItineraryParams params) {
        String destination = params == null || params.getDestination() == null ? "" : params.getDestination().trim();
        int days = params == null || params.getDays() == null ? 3 : Math.max(1, Math.min(params.getDays(), 7));
        int budget = params == null || params.getBudget() == null ? 3000 : params.getBudget();
        if (budget <= 0) {
            budget = 3000;
        }
        String interests = params == null || params.getInterests() == null ? "" : params.getInterests();

        Set<String> interestTags = parseInterests(interests);

        String centerLngLat = getCityCenter(destination);
        if (centerLngLat == null) {
            return buildErrorResult(functionName, "无法定位目的地：" + destination);
        }

        String[] parts = centerLngLat.split(",");
        double centerLng = Double.parseDouble(parts[0]);
        double centerLat = Double.parseDouble(parts[1]);

        List<PoiItem> attractions = searchPoi(destination, "风景名胜", centerLng, centerLat, 5);
        if (attractions.isEmpty()) {
            attractions = searchPoi(destination, "景点", centerLng, centerLat, 5);
        }
        List<PoiItem> restaurants = searchPoi(destination, "餐饮", centerLng, centerLat, 4);
        List<PoiItem> hotels = searchPoi(destination, "住宿服务", centerLng, centerLat, 3);
        if (hotels.isEmpty()) {
            hotels = searchPoi(destination, "酒店", centerLng, centerLat, 3);
        }

        JSONArray dailyPlans = new JSONArray();
        for (int d = 1; d <= days; d++) {
            JSONObject dayPlan = new JSONObject();
            dayPlan.put("day", d);
            dayPlan.put("date", "第" + d + "天");
            dayPlan.put("theme", generateDayTheme(d, days, interestTags));

            JSONArray spots = new JSONArray();
            if (d <= attractions.size()) {
                PoiItem attr = attractions.get(d - 1);
                spots.put(buildSpot(attr, "scenic", "上午", d == 1 ? 120 : 90));
            }
            if (d <= restaurants.size()) {
                PoiItem rest = restaurants.get(d - 1);
                spots.put(buildSpot(rest, "restaurant", "中午", 60));
            }
            if (d + days - 1 < attractions.size()) {
                PoiItem attr = attractions.get(d + days - 1);
                spots.put(buildSpot(attr, "scenic", "下午", d == days ? 150 : 120));
            }
            if (d <= restaurants.size() && d % 2 == 0) {
                PoiItem rest = restaurants.get(d - 1);
                spots.put(buildSpot(rest, "restaurant", "晚上", 90));
            } else if (d <= hotels.size()) {
                PoiItem hotel = hotels.get(d - 1);
                spots.put(buildSpot(hotel, "hotel", "住宿", 0));
            }

            dayPlan.put("spots", spots);
            dayPlan.put("estimatedCost", budget / days);
            dayPlan.put("transportTip", generateTransportTip(d, days, centerLng, centerLat));
            dailyPlans.put(dayPlan);
        }

        JSONObject result = new JSONObject();
        result.put("functionName", functionName);
        result.put("destination", destination);
        result.put("days", days);
        result.put("totalBudget", budget);
        result.put("center", centerLngLat);
        result.put("summary", String.format("已为你在%s规划%d天行程，总预算%s元，涵盖景点游览、美食推荐和住宿安排。",
                destination, days, budget));
        result.put("dailyPlans", dailyPlans);
        return result.toString();
    }

    private Set<String> parseInterests(String interests) {
        Set<String> tags = new HashSet<>();
        if (interests == null || interests.isBlank()) {
            return tags;
        }
        String text = interests.toLowerCase();
        if (text.contains("海") || text.contains("沙滩") || text.contains("海滨")) {
            tags.add("海");
        }
        if (text.contains("山") || text.contains("自然") || text.contains("徒步")) {
            tags.add("山");
        }
        if (text.contains("历史") || text.contains("古迹") || text.contains("文化")) {
            tags.add("历史");
        }
        if (text.contains("美食") || text.contains("吃")) {
            tags.add("美食");
        }
        if (text.contains("购物") || text.contains("逛街")) {
            tags.add("购物");
        }
        if (text.contains("夜景") || text.contains("夜")) {
            tags.add("夜");
        }
        return tags;
    }

    private String generateDayTheme(int day, int total, Set<String> interests) {
        if (interests.contains("海")) {
            return day == 1 ? "抵达日+海滨初体验" : "深度海滨游";
        }
        if (day == 1) {
            return "抵达日+城市初览";
        }
        if (day == total) {
            return "最后一天+返程准备";
        }
        if (interests.contains("历史")) {
            return "历史文化探索日";
        }
        if (interests.contains("美食")) {
            return "美食发现日";
        }
        return "精华景点游览日";
    }

    private String generateTransportTip(int day, int total, double lng, double lat) {
        if (day == 1) {
            return String.format("建议乘坐公共交通到达，提前用地图规划路线。起点坐标：%.4f, %.4f", lng, lat);
        }
        if (day == total) {
            return "今日为返程日，请提前收拾行李，注意航班/车次时间。";
        }
        return "建议早起错峰出行，景点间步行或打车更便捷。";
    }

    private String getCityCenter(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        try {
            String response = geoClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .queryParam("address", city)
                            .queryParam("city", city)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject json = new JSONObject(response);
            if (!"1".equals(json.optString("status"))) {
                return null;
            }
            JSONArray geocodes = json.optJSONArray("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                return null;
            }
            String location = geocodes.getJSONObject(0).optString("location", "");
            return location.isEmpty() ? null : location;
        } catch (Exception e) {
            log.warn("地理编码失败: {}", e.getMessage());
            return null;
        }
    }

    private List<PoiItem> searchPoi(String city, String type, double centerLng, double centerLat, int limit) {
        List<PoiItem> results = new ArrayList<>();
        try {
            String response = poiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .queryParam("keywords", type)
                            .queryParam("city", city)
                            .queryParam("citylimit", "true")
                            .queryParam("types", getAmapType(type))
                            .queryParam("offset", limit)
                            .queryParam("page", 1)
                            .queryParam("extensions", "all")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject json = new JSONObject(response);
            if (!"1".equals(json.optString("status"))) {
                return results;
            }
            JSONArray pois = json.optJSONArray("pois");
            if (pois == null) {
                return results;
            }

            for (int i = 0; i < Math.min(limit, pois.length()); i++) {
                JSONObject poi = pois.getJSONObject(i);
                String location = poi.optString("location", "");
                if (location.isEmpty()) {
                    continue;
                }
                String[] locParts = location.split(",");
                if (locParts.length < 2) {
                    continue;
                }
                PoiItem item = new PoiItem();
                item.name = poi.optString("name", "");
                item.address = poi.optString("address", poi.optString("business_area", ""));
                item.location = location;
                item.lng = Double.parseDouble(locParts[0]);
                item.lat = Double.parseDouble(locParts[1]);
                item.type = type;
                item.tag = poi.optString("type", "");
                item.tel = poi.optString("tel", "");
                results.add(item);
            }
        } catch (Exception e) {
            log.warn("POI搜索失败 [{}]: {}", type, e.getMessage());
        }
        return results;
    }

    private String getAmapType(String type) {
        return switch (type) {
            case "风景名胜" -> "110000";
            case "景点" -> "110000,110100,110200";
            case "餐饮" -> "050000";
            case "住宿服务", "酒店" -> "100000";
            default -> "";
        };
    }

    private JSONObject buildSpot(PoiItem item, String spotType, String timeSlot, int visitMinutes) {
        JSONObject spot = new JSONObject();
        spot.put("name", item.name);
        spot.put("type", spotType);
        spot.put("timeSlot", timeSlot);
        spot.put("duration", visitMinutes);
        spot.put("address", item.address);
        spot.put("location", item.location);
        spot.put("lng", item.lng);
        spot.put("lat", item.lat);
        spot.put("tag", item.tag);
        spot.put("tel", item.tel);
        return spot;
    }

    private String buildErrorResult(String functionName, String msg) {
        JSONObject result = new JSONObject();
        result.put("functionName", functionName);
        result.put("error", msg);
        return result.toString();
    }

    private static class PoiItem {
        String name;
        String address;
        String location;
        double lng;
        double lat;
        String type;
        String tag;
        String tel;
    }
}
