package com.neusoft.edu.neullmdev.service.llm;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.config.llm.LlmFriendlyErrors;
import com.neusoft.edu.neullmdev.config.llm.LlmMonoRetry;
import com.neusoft.edu.neullmdev.config.llm.LlmProperties;
import com.neusoft.edu.neullmdev.config.llm.LlmResponseParser;
import com.neusoft.edu.neullmdev.service.classroom.TeacherChatContextService;
import com.neusoft.edu.neullmdev.service.prompt.AgentSystemPrompt;
import com.neusoft.edu.neullmdev.service.prompt.reviewrhythm.ReviewRhythmPromptBundle;
import com.neusoft.edu.neullmdev.service.prompt.reviewrhythm.ReviewRhythmRequestDetector;
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
public class KimiChatServiceImpl implements KimiChatService {
    private final WebClient llmWebClient;
    private final LlmProperties llmProperties;
    private final AgentSystemPrompt agentSystemPrompt;
    private final KnowledgeService knowledgeService;
    private final TeacherChatContextService teacherChatContextService;

    public KimiChatServiceImpl(
            @Qualifier("llmWebClient") WebClient llmWebClient,
            LlmProperties llmProperties,
            AgentSystemPrompt agentSystemPrompt,
            KnowledgeService knowledgeService,
            TeacherChatContextService teacherChatContextService) {
        this.llmWebClient = llmWebClient;
        this.llmProperties = llmProperties;
        this.agentSystemPrompt = agentSystemPrompt;
        this.knowledgeService = knowledgeService;
        this.teacherChatContextService = teacherChatContextService;
    }

    @Override
    public Mono<String> chatCompletion(String query) {
        return chatCompletion(query, false);
    }

    @Override
    public Mono<String> chatCompletion(String query, boolean teacherMode) {
        return chatCompletion(query, teacherMode, AuthContext.get());
    }

    @Override
    public Mono<String> chatCompletion(String query, boolean teacherMode, AuthUser authUser) {
        return Mono.defer(() -> {
            String enrichedQuery = AuthContext.callWith(authUser, () -> {
                String q = knowledgeService.enrichUserQuery(query);
                if (teacherMode) {
                    q = teacherChatContextService.enrich(q, true);
                }
                return q;
            });
            return invokeLlm(enrichedQuery, teacherMode);
        });
    }

    private Mono<String> invokeLlm(String enrichedQuery, boolean teacherMode) {
        String systemPrompt = agentSystemPrompt.getPrompt(teacherMode);
        String userContent = enrichedQuery;
        if (ReviewRhythmRequestDetector.matches(enrichedQuery)) {
            systemPrompt = systemPrompt + "\n\n" + ReviewRhythmPromptBundle.systemAppend();
            userContent = ReviewRhythmPromptBundle.wrapUserTask(enrichedQuery);
        }

        JSONObject requestBody = new JSONObject()
                .put("model", llmProperties.resolveModel())
                .put("stream", false)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", systemPrompt))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", userContent)))
                .put("temperature", 0.6);

        log.debug("调用 Kimi，系统提示: {}...",
                systemPrompt.substring(0, Math.min(200, systemPrompt.length())));

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
                            log.warn("解析 LLM 响应为空，原始: {}", raw.substring(0, Math.min(500, raw.length())));
                            return "解析响应出错: 未识别模型返回格式";
                        }
                        log.debug("Kimi 返回内容长度: {}", content.length());
                        return content;
                    } catch (Exception e) {
                        log.error("解析 LLM 响应出错: {}，原始响应: {}", e.getMessage(), raw, e);
                        return "解析响应出错: " + e.getMessage();
                    }
                });

        return LlmMonoRetry.wrap(call, llmProperties)
                .onErrorResume(error -> {
                    log.error("LLM API 调用出错: {}", error.getMessage(), error);
                    return Mono.just(LlmFriendlyErrors.chatUserMessage(error));
                });
    }

    @Override
    public Mono<String> chatWithSystem(String systemPrompt, String userContent, double temperature) {
        JSONObject requestBody = new JSONObject()
                .put("model", llmProperties.resolveModel())
                .put("stream", false)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", systemPrompt))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", userContent)))
                .put("temperature", temperature);

        Mono<String> call = llmWebClient.post()
                .uri(llmProperties.resolveChatPath())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(String.class)
                .map(raw -> {
                    String content = LlmResponseParser.assistantContent(raw);
                    return content.isEmpty() ? "模型未返回有效内容" : content;
                });

        return LlmMonoRetry.wrap(call, llmProperties)
                .onErrorResume(error -> {
                    log.error("LLM 专用调用出错: {}", error.getMessage(), error);
                    return Mono.just(LlmFriendlyErrors.chatUserMessage(error));
                });
    }
}
