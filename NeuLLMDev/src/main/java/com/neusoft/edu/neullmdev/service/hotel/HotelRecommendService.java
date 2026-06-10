package com.neusoft.edu.neullmdev.service.hotel;

import com.neusoft.edu.neullmdev.dto.hotel.HotelSearchParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class HotelRecommendService {

    private static final String AMAP_BASE = "https://restapi.amap.com/v3/place/text";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int PAGE_SIZE = 20;

    @Value("${amap.api-key:}")
    private String amapApiKey;

    public String recommendHotels(HotelSearchParams params) {
        params = normalizeSearchParams(params);
        if (params.getCity() == null || params.getCity().trim().isEmpty()) {
            return buildErrorResult("请告诉我您想去哪个城市？");
        }
        if (amapApiKey == null || amapApiKey.trim().isEmpty() || "YOUR_AMAP_API_KEY".equals(amapApiKey.trim())) {
            return buildErrorResult("高德地图 API Key 未配置，请在 application.yml 中设置 amap.api-key。");
        }
        try {
            String keywords = buildKeywords(params.getKeywords());
            JSONArray hotels = searchHotelsByCity(params.getCity(), keywords);
            if (hotels == null || hotels.length() == 0) {
                return buildErrorResult("城市「" + params.getCity() + "」未搜索到酒店信息，请确认城市名称是否正确。");
            }
            return buildAmapResult(hotels, params);
        } catch (Exception e) {
            return buildErrorResult("酒店搜索接口异常（" + e.getMessage() + "），请稍后重试。");
        }
    }

    private JSONArray searchHotelsByCity(String city, String keywords) throws Exception {
        String url = buildSearchUrl(city, keywords, null);
        JSONArray pois = parsePois(httpGet(url));
        if (pois != null && pois.length() > 0) {
            return pois;
        }
        url = buildSearchUrl(city, null, "100000");
        return parsePois(httpGet(url));
    }

    private String buildSearchUrl(String city, String keywords, String types) {
        StringBuilder sb = new StringBuilder(AMAP_BASE).append("?");
        if (keywords != null && !keywords.trim().isEmpty()) {
            sb.append("keywords=").append(URLEncoder.encode(keywords.trim(), StandardCharsets.UTF_8)).append("&");
        }
        if (types != null && !types.trim().isEmpty()) {
            sb.append("types=").append(types.trim()).append("&");
        }
        sb.append("city=").append(URLEncoder.encode(city, StandardCharsets.UTF_8))
                .append("&offset=").append(PAGE_SIZE)
                .append("&page=1")
                .append("&extensions=base")
                .append("&key=").append(amapApiKey);
        return sb.toString();
    }

    private String httpGet(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "NeuLLM-TravelAssistant/1.0");
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("高德API HTTP错误: " + conn.getResponseCode());
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private JSONArray parsePois(String resp) {
        if (resp == null) {
            return null;
        }
        JSONObject json = new JSONObject(resp);
        if (!"1".equals(json.optString("status", "0"))) {
            return null;
        }
        return json.optJSONArray("pois");
    }

    private String buildKeywords(String userKeywords) {
        if (userKeywords == null || userKeywords.trim().isEmpty()) {
            return "宾馆酒店";
        }
        return userKeywords.trim() + " 住宿";
    }

    private String buildAmapResult(JSONArray pois, HotelSearchParams params) {
        long nights = calcNights(params.getCheckInDate(), params.getCheckOutDate());
        int guests = params.getGuests() != null ? params.getGuests() : 2;
        JSONArray hotelArr = new JSONArray();
        for (int i = 0; i < pois.length(); i++) {
            JSONObject poi = pois.getJSONObject(i);
            String location = poi.optString("location", "");
            String[] lngLat = location.split(",");
            JSONObject hotel = new JSONObject()
                    .put("hotelId", poi.optString("id", ""))
                    .put("hotelName", poi.optString("name", "未知酒店"))
                    .put("type", poi.optString("type", "宾馆酒店"))
                    .put("address", poi.optString("address", "暂无地址信息"))
                    .put("lng", lngLat.length > 0 ? lngLat[0] : "")
                    .put("lat", lngLat.length > 1 ? lngLat[1] : "")
                    .put("checkInDate", params.getCheckInDate())
                    .put("checkOutDate", params.getCheckOutDate())
                    .put("nights", nights)
                    .put("guests", guests)
                    .put("priceInfo", "详询酒店前台")
                    .put("contactPhone", poi.optString("tel", ""));
            hotelArr.put(hotel);
        }
        return new JSONObject()
                .put("function", "hotel_recommend")
                .put("functionName", "hotel_recommend")
                .put("status", "success")
                .put("dataSource", "amap_api")
                .put("totalCount", hotelArr.length())
                .put("hotels", hotelArr)
                .toString();
    }

    private HotelSearchParams normalizeSearchParams(HotelSearchParams params) {
        if (params == null) {
            params = new HotelSearchParams();
        }
        if (params.getCheckInDate() == null || params.getCheckInDate().trim().isEmpty()) {
            params.setCheckInDate(LocalDate.now().format(DATE_FMT));
        }
        if (params.getCheckOutDate() == null || params.getCheckOutDate().trim().isEmpty()) {
            try {
                params.setCheckOutDate(
                        LocalDate.parse(params.getCheckInDate(), DATE_FMT).plusDays(1).format(DATE_FMT));
            } catch (Exception e) {
                params.setCheckOutDate(LocalDate.now().plusDays(1).format(DATE_FMT));
            }
        }
        if (params.getGuests() == null || params.getGuests() <= 0) {
            params.setGuests(2);
        }
        return params;
    }

    private long calcNights(String checkIn, String checkOut) {
        try {
            long n = ChronoUnit.DAYS.between(
                    LocalDate.parse(checkIn, DATE_FMT),
                    LocalDate.parse(checkOut, DATE_FMT));
            return n > 0 ? n : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String buildErrorResult(String message) {
        return new JSONObject()
                .put("function", "hotel_recommend")
                .put("functionName", "hotel_recommend")
                .put("status", "error")
                .put("message", message)
                .toString();
    }
}
