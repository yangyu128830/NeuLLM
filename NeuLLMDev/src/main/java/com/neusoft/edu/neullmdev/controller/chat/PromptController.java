package com.neusoft.edu.neullmdev.controller.chat;

import com.neusoft.edu.neullmdev.dto.chat.ChatStreamRequest;
import com.neusoft.edu.neullmdev.service.agent.AgentService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/** 对话 SSE 入口（前端 POST /api/prompt/stream）。 */
@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final AgentService agentService;

    public PromptController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@RequestBody String body) {
        return agentService.streamChat(ChatStreamRequest.parse(body));
    }
}
