package vn.edu.crs.apigateway.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limiter for partner API keys.
 *
 * The state is intentionally local to one Gateway instance. A distributed
 * deployment should move this state to Redis or a dedicated rate-limit store.
 */
@Component
public class ApiKeyRateLimiter {

    private final ConcurrentHashMap<String, WindowCounter> counters =
            new ConcurrentHashMap<>();

    private final int maxRequests;
    private final long windowSeconds;

    public ApiKeyRateLimiter(
            @Value("${partner.rate-limit.max-requests:5}") int maxRequests,
            @Value("${partner.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public RateLimitResult tryConsume(String apiKey) {
        Instant now = Instant.now();

        WindowCounter counter = counters.compute(apiKey, (ignored, existing) -> {
            if (existing == null || !now.isBefore(existing.expiresAt())) {
                return new WindowCounter(
                        new AtomicInteger(1),
                        now.plusSeconds(windowSeconds)
                );
            }

            existing.requestCount().incrementAndGet();
            return existing;
        });

        int usedRequests = counter.requestCount().get();
        int remainingRequests = Math.max(0, maxRequests - usedRequests);
        long retryAfterSeconds = Math.max(
                1,
                Duration.between(now, counter.expiresAt()).toSeconds()
        );

        return new RateLimitResult(
                usedRequests <= maxRequests,
                maxRequests,
                remainingRequests,
                retryAfterSeconds
        );
    }

    private record WindowCounter(
            AtomicInteger requestCount,
            Instant expiresAt
    ) {
    }

    public record RateLimitResult(
            boolean allowed,
            int maxRequests,
            int remainingRequests,
            long retryAfterSeconds
    ) {
    }
}
