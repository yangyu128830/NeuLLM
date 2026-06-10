package com.neusoft.edu.neullmdev.config.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

/** 大模型 Chat Completions 配置（默认阿里云百炼 DashScope 兼容模式）。 */
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private String apiKey = "";

    /** 完整 Chat Completions URL，例如 https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private String model = "qwen-plus";

    private int maxRetries = 3;

    private long retryBackoffMs = 800L;

    /**
     * 对 HTTP 429（限流）额外重试次数；0 表示不重试。与 {@link #maxRetries} 独立：先按本项退避重试 429，再走普通重试（多为 5xx）。
     */
    private int retry429MaxAttempts = 2;

    /** 429 首次重试前的等待基数（毫秒），后续按指数退避并带上限。 */
    private long retry429InitialDelayMs = 4000L;

    public String resolveWebClientBaseUrl() {
        return splitEndpoint(baseUrl).base();
    }

    public String resolveChatPath() {
        return splitEndpoint(baseUrl).path();
    }

    public String resolveModel() {
        return model;
    }

    private static EndpointParts splitEndpoint(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            return new EndpointParts("https://dashscope.aliyuncs.com", "/compatible-mode/v1/chat/completions");
        }
        URI u = URI.create(endpointUrl.trim());
        String authority = u.getRawAuthority();
        if (authority == null) {
            return new EndpointParts("https://dashscope.aliyuncs.com", "/compatible-mode/v1/chat/completions");
        }
        String base = u.getScheme() + "://" + authority;
        String path = u.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (u.getQuery() != null) {
            path = path + "?" + u.getQuery();
        }
        return new EndpointParts(base, path);
    }

    private record EndpointParts(String base, String path) {}

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public int getRetry429MaxAttempts() {
        return retry429MaxAttempts;
    }

    public void setRetry429MaxAttempts(int retry429MaxAttempts) {
        this.retry429MaxAttempts = retry429MaxAttempts;
    }

    public long getRetry429InitialDelayMs() {
        return retry429InitialDelayMs;
    }

    public void setRetry429InitialDelayMs(long retry429InitialDelayMs) {
        this.retry429InitialDelayMs = retry429InitialDelayMs;
    }
}
