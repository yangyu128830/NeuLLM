package com.neusoft.edu.neullmdev.dto.food;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSearchParams {

    @JsonAlias({"keyword", "query", "q", "type"})
    private String keyword;

    @JsonAlias({"region", "city", "城市"})
    private String region;

    @JsonAlias({"adcode", "district_code"})
    private String adcode;

    private Double latitude;
    private Double longitude;

    private Integer radius;
    @JsonAlias({"page_size", "pageSize"})
    private Integer pageSize;

    @JsonAlias({"distance_order", "distanceOrder", "sort"})
    private String distanceOrder;

    public Map<String, Object> toMcpArguments() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (keyword != null) {
            m.put("keyword", keyword.trim());
        }
        if (region != null && !region.isBlank()) {
            m.put("region", region.trim());
        }
        if (adcode != null && !adcode.isBlank()) {
            m.put("adcode", adcode.trim());
        }
        if (latitude != null) {
            m.put("latitude", latitude);
        }
        if (longitude != null) {
            m.put("longitude", longitude);
        }
        if (radius != null) {
            m.put("radius", radius);
        }
        if (pageSize != null) {
            m.put("page_size", pageSize);
        }
        if (distanceOrder != null && !distanceOrder.isBlank()) {
            m.put("distance_order", distanceOrder.trim());
        }
        return m;
    }
}
