package com.example.shop.resilience;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthProbe {

    private final RedisCacheBypassGate redisCacheBypassGate;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedDelayString = "${redis.resilience.cache.probe-interval-ms:5000}")
    public void probeOpenCaches() {
        List<String> openCaches = redisCacheBypassGate.getOpenCacheNames();
        if (openCaches.isEmpty()) {
            return;
        }

        try {
            String pong = stringRedisTemplate.execute((RedisConnection connection) -> connection.ping());
            if (pong == null || !pong.equalsIgnoreCase("PONG")) {
                throw new IllegalStateException("Unexpected Redis ping response: " + pong);
            }
            openCaches.forEach(redisCacheBypassGate::recordSuccess);
            log.info("Redis health probe recovered open cache gates. caches={}", openCaches);
        } catch (RuntimeException e) {
            openCaches.forEach(cache -> redisCacheBypassGate.recordFailure(cache, e));
            log.warn("Redis health probe failed for open cache gates. caches={}, reason={}",
                    openCaches, e.getMessage());
        }
    }
}
