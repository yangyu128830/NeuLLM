package com.neusoft.edu.neullmdev.service.llm;

import com.neusoft.edu.neullmdev.auth.AuthUser;
import reactor.core.publisher.Mono;

/** Kimi（Moonshot Chat Completions）对话入口。 */
public interface KimiChatService {

    Mono<String> chatCompletion(String query);

    Mono<String> chatCompletion(String query, boolean teacherMode);

    Mono<String> chatCompletion(String query, boolean teacherMode, AuthUser authUser);

    /** 自定义系统提示（课堂批改辅助等专用场景，不走 Agent 工具路由）。 */
    Mono<String> chatWithSystem(String systemPrompt, String userContent, double temperature);
}
