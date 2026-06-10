package com.neusoft.edu.neullmdev.service.llm;

import com.neusoft.edu.neullmdev.config.llm.LlmMonoRetry;
import com.neusoft.edu.neullmdev.config.llm.LlmProperties;
import com.neusoft.edu.neullmdev.config.llm.LlmResponseParser;
import com.neusoft.edu.neullmdev.service.prompt.WeatherFinalAnswerPrompt;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class FinalAnswerServiceImpl implements FinalAnswerService {

    private final WebClient llmWebClient;
    private final LlmProperties llmProperties;
    private final WeatherFinalAnswerPrompt weatherFinalAnswerPrompt;

    public FinalAnswerServiceImpl(
            @Qualifier("llmWebClient") WebClient llmWebClient,
            LlmProperties llmProperties,
            WeatherFinalAnswerPrompt weatherFinalAnswerPrompt) {
        this.llmWebClient = llmWebClient;
        this.llmProperties = llmProperties;
        this.weatherFinalAnswerPrompt = weatherFinalAnswerPrompt;
    }

    @Override
    public Mono<String> finalWeatherAnswer(String userQuestion, String weatherApiResult) {
        String uq = userQuestion == null ? "" : userQuestion.trim();
        String finalQuery = weatherFinalAnswerPrompt.getPrompt()
                + "\n【用户原话（可点出出发日/目的地，勿整句复述）】\n" + uq
                + "\n【天气接口原文】\n" + (weatherApiResult == null ? "" : weatherApiResult);
        JSONObject requestBody = new JSONObject()
                .put("model", llmProperties.resolveModel())
                .put("stream", false)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", "你是用户身边温柔亲近的朋友，一起在出门前看一眼天气。只根据天气接口原文作答：用暖暖的口语中文写「天气实况与体感」「穿衣」「出门小叮嘱」三层意思即可，总字数建议不超过约 200 字；像在微信里关心 TA，不要用公告体；不要地图、不要景点行程、不要酒店推荐；不臆造数据里没有的信息。"))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", finalQuery)))
                .put("temperature", 0.72);

        log.debug("调用 Kimi 天气最终润色，输入提示: {}", finalQuery);

        Mono<String> call = llmWebClient.post()
                .uri(llmProperties.resolveChatPath())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(String.class)
                .map(raw -> {
                    try {
                        String content = LlmResponseParser.assistantContent(raw);
                        if (content.isEmpty()) {
                            log.warn("解析最终回答为空，原始: {}", raw.substring(0, Math.min(500, raw.length())));
                            return "解析响应出错: 未识别模型返回格式";
                        }
                        log.debug("Kimi 天气润色完成");
                        return content;
                    } catch (Exception e) {
                        log.error("解析 LLM 最终回答出错: {}，原始响应: {}", e.getMessage(), raw, e);
                        return "解析响应出错: " + e.getMessage();
                    }
                });

        return LlmMonoRetry.wrap(call, llmProperties)
                .onErrorResume(error -> {
                    log.error("LLM 最终回答调用出错: {}", error.getMessage(), error);
                    return Mono.just(weatherApiResult != null ? weatherApiResult : "");
                });
    }
}
