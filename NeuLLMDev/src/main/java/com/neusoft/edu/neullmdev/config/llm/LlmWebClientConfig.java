package com.neusoft.edu.neullmdev.config.llm;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmWebClientConfig {

    @Bean(name = "llmWebClient")
    public WebClient llmWebClient(WebClient.Builder webClientBuilder, LlmProperties props) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs());

        WebClient.Builder b = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(props.resolveWebClientBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String key = props.getApiKey();
        if (key != null && !key.isBlank()) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key);
        }
        return b.build();
    }
}
