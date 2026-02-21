package com.example.shop.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RedisCacheBypassGate {

    enum GateStatus {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private static class GateState {
        private GateStatus status = GateStatus.CLOSED;
        private int consecutiveFailures = 0;
        private long openedAtMillis = 0L;
    }

    private final int failureThreshold;
    private final long openDurationMillis;
    private final Map<String, GateState> states = new ConcurrentHashMap<>();

    public RedisCacheBypassGate(
            @Value("${redis.resilience.cache.failure-threshold:3}") int failureThreshold,
            @Value("${redis.resilience.cache.open-duration-ms:30000}") long openDurationMillis
    ) {
        this.failureThreshold = Math.max(1, failureThreshold);
        this.openDurationMillis = Math.max(10L, openDurationMillis);
    }

    public boolean allowCache(String cacheName) {
        GateState state = stateFor(cacheName);
        synchronized (state) {
            if (state.status == GateStatus.OPEN) {
                if (isOpenWindowElapsed(state)) {
                    state.status = GateStatus.HALF_OPEN;
                    log.info("Redis cache gate moved to HALF_OPEN. cache={}", cacheName);
                    return true;
                }
                return false;
            }
            return true;
        }
    }

    public void recordFailure(String cacheName, Throwable throwable) {
        GateState state = stateFor(cacheName);
        synchronized (state) {
            if (state.status == GateStatus.HALF_OPEN) {
                open(state);
                log.warn("Redis cache gate returned to OPEN from HALF_OPEN. cache={}, reason={}",
                        cacheName, throwable != null ? throwable.getMessage() : "unknown");
                return;
            }

            state.consecutiveFailures++;
            if (state.consecutiveFailures >= failureThreshold) {
                open(state);
                log.warn("Redis cache gate opened. cache={}, threshold={}, reason={}",
                        cacheName, failureThreshold, throwable != null ? throwable.getMessage() : "unknown");
            }
        }
    }

    public void recordSuccess(String cacheName) {
        GateState state = stateFor(cacheName);
        synchronized (state) {
            if (state.status != GateStatus.CLOSED || state.consecutiveFailures > 0) {
                log.info("Redis cache gate closed. cache={}, previousStatus={}", cacheName, state.status);
            }
            state.status = GateStatus.CLOSED;
            state.consecutiveFailures = 0;
            state.openedAtMillis = 0L;
        }
    }

    public List<String> getOpenCacheNames() {
        return states.entrySet().stream()
                .filter(entry -> entry.getValue().status != GateStatus.CLOSED)
                .map(Map.Entry::getKey)
                .toList();
    }

    GateStatus getStatus(String cacheName) {
        return stateFor(cacheName).status;
    }

    private GateState stateFor(String cacheName) {
        return states.computeIfAbsent(cacheName, key -> new GateState());
    }

    private boolean isOpenWindowElapsed(GateState state) {
        return Instant.now().toEpochMilli() - state.openedAtMillis >= openDurationMillis;
    }

    private void open(GateState state) {
        state.status = GateStatus.OPEN;
        state.consecutiveFailures = 0;
        state.openedAtMillis = Instant.now().toEpochMilli();
    }
}
