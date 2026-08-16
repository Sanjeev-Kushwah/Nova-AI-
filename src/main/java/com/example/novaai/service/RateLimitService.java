package com.example.novaai.service;

import com.example.novaai.config.AppConfig;
import com.example.novaai.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimitService {

    private final int requestsPerMinute;
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitService(AppConfig appConfig) {
        this.requestsPerMinute = appConfig.getRateLimit().getAuthenticatedRpm();
    }

    public void checkRateLimit(UUID userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, this::newBucket);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for user {}", userId);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Please wait a moment and try again."
            );
        }
    }

    private Bucket newBucket(UUID userId) {
        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
