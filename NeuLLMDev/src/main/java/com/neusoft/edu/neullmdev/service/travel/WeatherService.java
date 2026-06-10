package com.neusoft.edu.neullmdev.service.travel;

import com.neusoft.edu.neullmdev.dto.travel.WeatherParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WeatherService {

    private static final String WEATHER_BASE_URL = "https://restapi.amap.com/v3/weather/weatherInfo";
    private static final String GEO_BASE_URL = "https://restapi.amap.com/v3/geocode/geo";

    private final WebClient webClient;
    private final WebClient geoClient;

    @Value("${amap.api-key:}")
    private String apiKey;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(WEATHER_BASE_URL).build();
        this.geoClient = webClientBuilder.baseUrl(GEO_BASE_URL).build();
    }

    public ToolResult query(WeatherParams params) {
        String text = getWeather("get_current_weather", params).block();
        return McpToolSupport.fromJsonString("get_current_weather", text);
    }

    public Mono<String> getWeather(String functionName, WeatherParams params) {
        log.debug("执行天气查询函数: {}", functionName);

        if (apiKey == null || apiKey.isBlank()) {
            return Mono.just("天气功能未配置高德Key：请在 application.yml 中设置 amap.api-key");
        }

        String adcode = params == null ? null : params.getAdcode();
        String location = params == null ? null : params.getLocation();

        Mono<String> adcodeMono = (adcode != null && !adcode.isBlank())
                ? Mono.just(adcode.trim())
                : resolveAdcodeByLocation(location);

        return adcodeMono.flatMap(code -> webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .queryParam("city", code)
                        .queryParam("extensions", "base")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("天气API请求失败，状态码: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("天气API请求失败: " + errorBody)));
                })
                .bodyToMono(String.class)
                .map(response -> parseWeatherResponse(response, location))
                .doOnError(error -> log.error("天气查询异常: {}", error.getMessage(), error))
                .onErrorResume(error -> Mono.just("天气查询异常：" + error.getMessage()))
        );
    }

    private String parseWeatherResponse(String response, String location) {
        JSONObject jsonResponse = new JSONObject(response);
        if (!"1".equals(jsonResponse.getString("status"))) {
            String errorInfo = jsonResponse.getString("info");
            log.warn("天气数据获取失败: {}", errorInfo);
            return "天气数据获取失败：" + errorInfo;
        }
        JSONArray lives = jsonResponse.getJSONArray("lives");
        if (lives.isEmpty()) {
            return "未找到该地区的天气信息";
        }
        JSONObject weatherData = lives.getJSONObject(0);
        String weather = weatherData.getString("weather");
        String temperature = weatherData.getString("temperature");
        String winddirection = weatherData.getString("winddirection");
        String windpower = weatherData.getString("windpower");
        String humidity = weatherData.optString("humidity", "未知");
        String place = (location == null || location.isBlank())
                ? weatherData.optString("city", "该地区")
                : location;
        String result = String.format("%s：%s，温度%s℃，%s风%s级，湿度%s%%",
                place, weather, temperature, winddirection, windpower, humidity);
        log.debug("天气查询结果: {}", result);
        return result;
    }

    private Mono<String> resolveAdcodeByLocation(String location) {
        if (location == null || location.isBlank()) {
            return Mono.error(new IllegalArgumentException("缺少location参数，无法查询天气"));
        }
        return geoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .queryParam("address", location.trim())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(raw -> {
                    JSONObject json = new JSONObject(raw);
                    if (!"1".equals(json.optString("status"))) {
                        throw new RuntimeException("地理编码失败：" + json.optString("info", "未知错误"));
                    }
                    JSONArray geocodes = json.optJSONArray("geocodes");
                    if (geocodes == null || geocodes.isEmpty()) {
                        throw new RuntimeException("未找到该地点的adcode：" + location);
                    }
                    String adcode = geocodes.getJSONObject(0).optString("adcode", "");
                    if (adcode.isBlank()) {
                        throw new RuntimeException("地点未返回adcode：" + location);
                    }
                    return adcode;
                });
    }
}
