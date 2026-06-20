package com.faisalaffan.javara.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "javara")
public record JavaraProperties(T24 t24, Security security) {

    public record T24(
        String defaultAdapter,
        List<String> adapters,
        Ofs ofs,
        Resilience resilience
    ) {
        public record Ofs(String endpoint, String username, String password, String authType) {}
        public record Resilience(
            Retry retry,
            CircuitBreaker circuitBreaker,
            TimeLimiter timeLimiter
        ) {
            public record Retry(int maxAttempts, String backoff, String maxDelay) {}
            public record CircuitBreaker(int failureRateThreshold, String waitDurationInOpen, int slidingWindowSize) {}
            public record TimeLimiter(String ofs, String tafj) {}
        }
    }

    public record Security(String authMode) {}
}
