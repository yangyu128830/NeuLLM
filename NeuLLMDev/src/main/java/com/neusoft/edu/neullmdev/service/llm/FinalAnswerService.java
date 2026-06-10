package com.neusoft.edu.neullmdev.service.llm;

import reactor.core.publisher.Mono;

public interface FinalAnswerService {

    /**
     * @param userQuestion   用户原话（用于识别出发日语气等，勿复述冗长）
     * @param weatherApiResult 天气接口返回的原文
     */
    Mono<String> finalWeatherAnswer(String userQuestion, String weatherApiResult);
}
