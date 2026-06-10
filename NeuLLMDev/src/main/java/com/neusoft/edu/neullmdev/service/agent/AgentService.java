package com.neusoft.edu.neullmdev.service.agent;

import com.neusoft.edu.neullmdev.dto.chat.ChatStreamRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * 多智能体调度入口：解析用户提示词并驱动 LLM + 工具调用（function calling），SSE 流式输出。
 */
public interface AgentService {

    Flux<ServerSentEvent<Object>> streamChat(String userInput);

    Flux<ServerSentEvent<Object>> streamChat(ChatStreamRequest request);
}
