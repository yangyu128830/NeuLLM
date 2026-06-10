package com.neusoft.edu.neullmdev.config.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmWebClientConfig {

    @Bean(name = "llmWebClient")
    public WebClient llmWebClient(WebClient.Builder webClientBuilder, LlmProperties props) {
        WebClient.Builder b = webClientBuilder
                .baseUrl(props.resolveWebClientBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String key = props.getApiKey();
        if (key != null && !key.isBlank()) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key);
        }
        return b.build();
    }
}
