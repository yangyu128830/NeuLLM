package com.neusoft.edu.neullmdev.config.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/** Moonshot WebClient 调用的统一重试：先处理 429，再处理非 4xx / 5xx 等。 */
@Slf4j
public final class LlmMonoRetry {

    private LlmMonoRetry() {
    }

    public static <T> Mono<T> wrap(Mono<T> mono, LlmProperties props) {
        return with429ThenStandardRetry(mono, props);
    }

    private static <T> Mono<T> with429ThenStandardRetry(Mono<T> mono, LlmProperties props) {
        Mono<T> step = mono;
        int r429 = props.getRetry429MaxAttempts();
        if (r429 > 0) {
            long initial = Math.max(1000L, props.getRetry429InitialDelayMs());
            step = mono.retryWhen(
                    Retry.backoff(r429, Duration.ofMillis(initial))
                            .maxBackoff(Duration.ofSeconds(60))
                            .jitter(0.25)
                            .filter(err -> err instanceof WebClientResponseException ex
                                    && ex.getStatusCode().value() == 429)
                            .doBeforeRetry(sig ->
                                    log.warn("Moonshot 429 限流，第 {} 次退避重试…", sig.totalRetries() + 1))
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
        }
        return withStandardRetry(step, props);
    }

    private static <T> Mono<T> withStandardRetry(Mono<T> mono, LlmProperties props) {
        int max = props.getMaxRetries();
        if (max <= 0) {
            return mono;
        }
        long backoff = Math.max(1L, props.getRetryBackoffMs());
        return mono.retryWhen(
                Retry.backoff(max, Duration.ofMillis(backoff))
                        .filter(err -> !(err instanceof WebClientResponseException ex
                                && ex.getStatusCode().is4xxClientError()))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}
