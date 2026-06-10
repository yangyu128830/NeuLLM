package com.neusoft.edu.neullmdev.service.food;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.food.FoodSearchParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import com.neusoft.edu.neullmdev.tool.common.TencentMapSigUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class FoodSearchService {

    private static final String PLACE_SEARCH_PATH = "/ws/place/v1/search";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${tencent.map.key:}")
    private String key;

    @Value("${tencent.map.secret:}")
    private String secret;

    public FoodSearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://apis.map.qq.com").build();
        this.objectMapper = objectMapper;
    }

    public ToolResult searchAsToolResult(FoodSearchParams params) {
        Map<String, Object> content = search(params);
        try {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("functionName", "food_search");
            out.put("keyword", params.getKeyword());
            out.put("region", params.getRegion() != null && !params.getRegion().isBlank() ? params.getRegion().trim() : "");
            if (params.getAdcode() != null && StringUtils.hasText(params.getAdcode().trim())) {
                out.put("adcode", params.getAdcode().trim());
            }
            if (params.getLatitude() != null) {
                out.put("latitude", params.getLatitude());
            }
            if (params.getLongitude() != null) {
                out.put("longitude", params.getLongitude());
            }
            if (Boolean.TRUE.equals(content.get("error"))) {
                out.put("error", true);
                out.put("message", String.valueOf(content.getOrDefault("message", "未知错误")));
                out.put("pois", List.of());
                out.put("count", 0);
                return McpToolSupport.fromJsonString("food_search", McpToolSupport.writeJson(objectMapper, out));
            }
            out.put("count", content.getOrDefault("count", 0));
            Object pois = content.get("pois");
            out.put("pois", pois != null ? pois : List.of());
            if (content.containsKey("distance_order")) {
                out.put("distance_order", content.get("distance_order"));
            }
            out.put("error", false);
            return McpToolSupport.fromJsonString("food_search", McpToolSupport.writeJson(objectMapper, out));
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("functionName", "food_search");
            err.put("error", true);
            err.put("message", "美食搜索失败: " + e.getMessage());
            err.put("pois", List.of());
            err.put("count", 0);
            return McpToolSupport.fromJsonString("food_search", McpToolSupport.writeJson(objectMapper, err));
        }
    }

    public Map<String, Object> search(FoodSearchParams params) {
        String keyword = params.getKeyword();
        String region = params.getRegion();
        String adcode = params.getAdcode();
        Double latitude = params.getLatitude();
        Double longitude = params.getLongitude();
        int radius = params.getRadius() != null ? params.getRadius() : 500;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : 10;
        String distanceOrder = params.getDistanceOrder();

        if (!StringUtils.hasText(key)) {
            return Map.of("error", true, "message", "未配置 tencent.map.key（或环境变量 TENCENT_MAP_KEY）");
        }
        if (!StringUtils.hasText(secret)) {
            return Map.of("error", true, "message", "未配置 tencent.map.secret（控制台 SK，或环境变量 TENCENT_MAP_SK）");
        }
        if (!StringUtils.hasText(keyword)) {
            return Map.of("error", true, "message", "keyword 不能为空");
        }

        final boolean hasCoord = latitude != null && longitude != null;
        final String order = normalizeOrder(distanceOrder);

        String boundary;
        boolean requestOrderByDistance = false;

        if (hasCoord && !StringUtils.hasText(region) && !StringUtils.hasText(adcode)) {
            int r = clamp(radius, 10, 1000);
            boundary = "nearby(" + latitude + "," + longitude + "," + r + ",1)";
            requestOrderByDistance = true;
        } else if (StringUtils.hasText(adcode)) {
            String code = adcode.trim();
            if (hasCoord) {
                boundary = "region(" + code + ",1," + latitude + "," + longitude + ")";
                requestOrderByDistance = true;
            } else {
                boundary = "region(" + code + ",1)";
            }
        } else if (StringUtils.hasText(region)) {
            String name = region.trim();
            if (hasCoord) {
                boundary = "region(" + name + ",1," + latitude + "," + longitude + ")";
                requestOrderByDistance = true;
            } else {
                boundary = "region(" + name + ",1)";
            }
        } else {
            return Map.of("error", true, "message", "请提供 region 或 adcode，或提供 latitude+longitude 做周边搜索");
        }

        int ps = clamp(pageSize, 1, 20);

        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("boundary", boundary);
        signParams.put("keyword", keyword.trim());
        signParams.put("key", key.trim());
        signParams.put("output", "json");
        signParams.put("page_size", String.valueOf(ps));
        if (requestOrderByDistance) {
            signParams.put("orderby", "_distance");
        }

        String sig = TencentMapSigUtil.computeGetSig(secret.trim(), PLACE_SEARCH_PATH, signParams);

        UriComponentsBuilder ub = UriComponentsBuilder.fromUriString("https://apis.map.qq.com" + PLACE_SEARCH_PATH)
                .queryParam("boundary", signParams.get("boundary"))
                .queryParam("keyword", signParams.get("keyword"))
                .queryParam("key", signParams.get("key"))
                .queryParam("output", signParams.get("output"))
                .queryParam("page_size", signParams.get("page_size"));
        if (requestOrderByDistance) {
            ub.queryParam("orderby", "_distance");
        }
        URI uri = ub.queryParam("sig", sig).encode().build().toUri();

        try {
            String body = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));
            return simplifyResponse(body, order, requestOrderByDistance || hasCoord);
        } catch (Exception e) {
            return Map.of("error", true, "message", "请求腾讯地图失败: " + e.getMessage());
        }
    }

    private static String normalizeOrder(String distanceOrder) {
        if (distanceOrder == null || distanceOrder.isBlank()) {
            return "asc";
        }
        String raw = distanceOrder.trim();
        String s = raw.toLowerCase();
        if ("desc".equals(s) || "far_first".equals(s) || "far".equals(s)) {
            return "desc";
        }
        if (raw.contains("由远") || raw.contains("远到近") || raw.contains("远及近")) {
            return "desc";
        }
        if (raw.contains("由近") || raw.contains("近到远")) {
            return "asc";
        }
        return "asc";
    }

    private Map<String, Object> simplifyResponse(String body, String distanceOrder, boolean sortByDistance) {
        try {
            JsonNode root = objectMapper.readTree(body);
            int status = root.path("status").asInt(-1);
            if (status != 0) {
                return Map.of(
                        "error", true,
                        "status", status,
                        "message", root.path("message").asText("unknown")
                );
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (JsonNode item : root.withArray("data")) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.path("id").asText(""));
                row.put("title", item.path("title").asText(""));
                row.put("address", item.path("address").asText(""));
                row.put("tel", item.path("tel").asText(""));
                row.put("category", item.path("category").asText(""));
                if (item.hasNonNull("location")) {
                    row.put("lat", item.path("location").path("lat").asDouble());
                    row.put("lng", item.path("location").path("lng").asDouble());
                }
                if (item.has("_distance")) {
                    row.put("distance_m", item.path("_distance").asDouble());
                }
                list.add(row);
            }

            if (sortByDistance && list.stream().anyMatch(m -> m.get("distance_m") instanceof Number)) {
                list.sort((a, b) -> compareDistance(a, b, distanceOrder));
            }

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("count", root.path("count").asInt(list.size()));
            out.put("pois", list);
            out.put("distance_order", "desc".equals(distanceOrder) ? "far_first" : "near_first");
            return out;
        } catch (Exception e) {
            return Map.of("error", true, "message", "解析响应失败: " + e.getMessage(), "raw", body);
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int compareDistance(Map<String, Object> a, Map<String, Object> b, String distanceOrder) {
        Double da = toDistance(a.get("distance_m"));
        Double db = toDistance(b.get("distance_m"));
        if (da == null && db == null) {
            return 0;
        }
        if (da == null) {
            return 1;
        }
        if (db == null) {
            return -1;
        }
        int cmp = Double.compare(da, db);
        return "desc".equals(distanceOrder) ? -cmp : cmp;
    }

    private static Double toDistance(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return null;
    }
}
