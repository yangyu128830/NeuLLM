package com.neusoft.edu.neullmdev.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.dto.chat.ChatStreamRequest;
import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskService;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomToolResponseFactory;
import com.neusoft.edu.neullmdev.service.llm.FinalAnswerService;
import com.neusoft.edu.neullmdev.service.llm.KimiChatService;
import com.neusoft.edu.neullmdev.service.mcp.McpPayloads;
import com.neusoft.edu.neullmdev.service.mcp.McpService;
import com.neusoft.edu.neullmdev.config.llm.LlmFriendlyErrors;
import com.neusoft.edu.neullmdev.service.agent.support.ProcessJSONStringTools;
import com.neusoft.edu.neullmdev.service.agent.support.TravelPrepCardHints;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private final KimiChatService kimiChatService;
    private final McpService mcpService;
    private final FinalAnswerService finalAnswer;
    private final ProcessJSONStringTools processJSONStringTools;
    private final IntentReconciler intentReconciler;
    private final ObjectMapper objectMapper;
    private final ClassroomTaskService taskService;

    public AgentServiceImpl(KimiChatService kimiChatService,
                            McpService mcpService,
                            FinalAnswerService finalAnswer,
                            ProcessJSONStringTools processJSONStringTools,
                            IntentReconciler intentReconciler,
                            ObjectMapper objectMapper,
                            ClassroomTaskService taskService) {
        this.kimiChatService = kimiChatService;
        this.mcpService = mcpService;
        this.finalAnswer = finalAnswer;
        this.processJSONStringTools = processJSONStringTools;
        this.intentReconciler = intentReconciler;
        this.objectMapper = objectMapper;
        this.taskService = taskService;
    }

    @Override
    public Flux<ServerSentEvent<Object>> streamChat(String userInput) {
        return streamChat(new ChatStreamRequest(userInput, false));
    }

    @Override
    public Flux<ServerSentEvent<Object>> streamChat(ChatStreamRequest request) {
        String userInput = request != null ? request.message() : "";
        boolean teacherMode = request != null && request.teacherMode();
        final AuthUser authUser = AuthContext.get();
        log.info("用户输入 (teacherMode={}): {}", teacherMode, userInput);

        Mono<String> llmMono = kimiChatService.chatCompletion(userInput, teacherMode, authUser);

        Flux<ServerSentEvent<Object>> generationStart = Flux.just(ServerSentEvent.builder()
                .event("generation_start")
                .data("{}")
                .build());

        Flux<ServerSentEvent<Object>> afterLlm = llmMono.flatMapMany(answer -> {
                    FunctionCall parsed = processJSONStringTools.parseFunctionCall(answer);
                    final FunctionCall functionCall = AuthContext.callWith(authUser,
                            () -> intentReconciler.reconcile(userInput, parsed, teacherMode, authUser));
                    log.debug("函数调用: {}", functionCall);
                    if (functionCall == null || functionCall.getName() == null || functionCall.getName().isEmpty()) {
                        String body = answer != null ? answer : "";
                        if (body.length() > 280) {
                            Flux<ServerSentEvent<Object>> chunks = Flux.fromIterable(chunkUtf16(body, 64))
                                    .delayElements(Duration.ofMillis(14))
                                    .map(chunk -> ServerSentEvent.builder()
                                            .event("direct_answer_chunk")
                                            .data(new JSONObject().put("content", chunk).toString())
                                            .build());
                            Flux<ServerSentEvent<Object>> done = Flux.just(ServerSentEvent.builder()
                                    .event("direct_answer_done")
                                    .data("{}")
                                    .build());
                            return Flux.concat(chunks, done);
                        }
                        return Flux.just(ServerSentEvent.builder()
                                .event("direct_answer")
                                .data(body)
                                .build());
                    }

                    String toolName = functionCall.getName();
                    if (teacherMode && ClassroomToolResponseFactory.needsTeacherPreview(toolName)) {
                        String previewPayload = AuthContext.callWith(authUser, () -> buildPreviewPayload(
                                toolName, functionCall.getParams()));
                        return Flux.just(ServerSentEvent.builder()
                                .event("function_result")
                                .data(previewPayload)
                                .build());
                    }
                    return Mono.fromCallable(() -> AuthContext.callWith(authUser, () -> mcpService.callTool(
                                    toolName,
                                    functionCall.getParams(),
                                    McpCallContext.preview(userInput))))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMapMany(toolResult -> {
                                if ("get_current_weather".equalsIgnoreCase(functionCall.getName())) {
                                    String result = McpPayloads.toAgentJson(toolResult, objectMapper);
                                    log.debug("天气结果: {}", result);
                                    return finalAnswer.finalWeatherAnswer(userInput, result)
                                            .flatMapMany(finalAnswerStr -> {
                                                JSONObject startPayload = TravelPrepCardHints.weatherStartPayload(userInput);
                                                startPayload.put("total_length", finalAnswerStr.length());
                                                startPayload.put("start_time", System.currentTimeMillis());
                                                if (result != null && !result.isBlank()) {
                                                    String hint = result.length() > 400 ? result.substring(0, 400) : result;
                                                    startPayload.put("weather_raw", hint);
                                                }
                                                Flux<ServerSentEvent<Object>> startEvent = Flux.just(
                                                        ServerSentEvent.builder()
                                                                .event("weather_start")
                                                                .data(startPayload.toString())
                                                                .build()
                                                );

                                                Flux<ServerSentEvent<Object>> charStream = Flux.fromIterable(chunkUtf16(finalAnswerStr, 6))
                                                        .delayElements(Duration.ofMillis(18))
                                                        .map(chunk -> ServerSentEvent.builder()
                                                                .event("weather_chunk")
                                                                .data(new JSONObject()
                                                                        .put("content", chunk)
                                                                        .put("char_type",
                                                                                Character.isWhitespace(chunk.charAt(0)) ? "space" : "text")
                                                                        .toString())
                                                                .build());

                                                Flux<ServerSentEvent<Object>> endEvent = Flux.just(
                                                        ServerSentEvent.builder()
                                                                .event("weather_complete")
                                                                .data(new JSONObject()
                                                                        .put("end_time", System.currentTimeMillis())
                                                                        .toString())
                                                                .build()
                                                );

                                                return Flux.concat(startEvent, charStream, endEvent);
                                            });
                                }
                                String payload = toFunctionResultPayload(toolName, toolResult, teacherMode);
                                return Flux.just(ServerSentEvent.builder()
                                        .event("function_result")
                                        .data(payload)
                                        .build());
                            })
                            .onErrorResume(e -> Flux.just(ServerSentEvent.builder()
                                    .event("function_result")
                                    .data(ClassroomToolResponseFactory.errorJson(e.getMessage()))
                                    .build()));
                });

        return Flux.concat(generationStart, afterLlm)
                .onErrorResume(error -> {
                    log.warn("AgentService 流式对话异常: {}", error.getMessage());
                    String msg = LlmFriendlyErrors.chatUserMessage(error);
                    return Flux.just(
                            ServerSentEvent.builder()
                                    .event("error")
                                    .data(msg)
                                    .build());
                });
    }

    private static List<String> chunkUtf16(String s, int chunkSize) {
        if (s == null || s.isEmpty()) {
            return List.of();
        }
        int step = Math.max(1, chunkSize);
        List<String> out = new ArrayList<>((s.length() + step - 1) / step);
        for (int i = 0; i < s.length(); i += step) {
            out.add(s.substring(i, Math.min(i + step, s.length())));
        }
        return out;
    }

    private String buildPreviewPayload(String toolName, Map<String, Object> params) {
        if ("create_classroom_task".equalsIgnoreCase(toolName)) {
            return ClassroomToolResponseFactory.createTaskPreview(params, objectMapper);
        }
        String taskId = paramString(params, "taskId");
        String title = taskId;
        try {
            title = taskService.taskDetail(taskId).title();
        } catch (Exception ignored) {
            // 使用 taskId 作为标题占位
        }
        return ClassroomToolResponseFactory.publishTaskPreview(taskId, title);
    }

    private static String paramString(Map<String, Object> params, String key) {
        if (params == null) {
            return "";
        }
        Object v = params.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private String toFunctionResultPayload(String toolName, ToolResult toolResult, boolean teacherMode) {
        if (teacherMode && ClassroomToolResponseFactory.isClassroomTool(toolName)) {
            return ClassroomToolResponseFactory.toFrontendJson(toolResult, objectMapper);
        }
        try {
            Object data = toolResult.getData();
            if (data instanceof String s && !s.isBlank()) {
                return s;
            }
            if (data != null) {
                return objectMapper.writeValueAsString(data);
            }
            return objectMapper.writeValueAsString(Map.of(
                    "functionName", toolResult.getToolName(),
                    "message", toolResult.getSummary() != null ? toolResult.getSummary() : ""
            ));
        } catch (Exception e) {
            return "{\"error\":\"JSON序列化失败\"}";
        }
    }
}
