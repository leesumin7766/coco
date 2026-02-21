package com.example.shop.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisCacheBypassGateTest {

    @Test
    void opensAfterThresholdAndTransitionsToHalfOpenThenClosed() throws InterruptedException {
        RedisCacheBypassGate gate = new RedisCacheBypassGate(2, 80);
        String cacheName = "productDetail";

        gate.recordFailure(cacheName, new RuntimeException("fail-1"));
        assertTrue(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.CLOSED, gate.getStatus(cacheName));

        gate.recordFailure(cacheName, new RuntimeException("fail-2"));
        assertFalse(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.OPEN, gate.getStatus(cacheName));

        Thread.sleep(100);
        assertTrue(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.HALF_OPEN, gate.getStatus(cacheName));

        gate.recordSuccess(cacheName);
        assertTrue(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.CLOSED, gate.getStatus(cacheName));
    }

    @Test
    void halfOpenFailureReopensGate() throws InterruptedException {
        RedisCacheBypassGate gate = new RedisCacheBypassGate(2, 80);
        String cacheName = "productDetail";

        gate.recordFailure(cacheName, new RuntimeException("fail-1"));
        gate.recordFailure(cacheName, new RuntimeException("fail-2"));
        assertFalse(gate.allowCache(cacheName));

        Thread.sleep(100);
        assertTrue(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.HALF_OPEN, gate.getStatus(cacheName));

        gate.recordFailure(cacheName, new RuntimeException("half-open-fail"));
        assertFalse(gate.allowCache(cacheName));
        assertEquals(RedisCacheBypassGate.GateStatus.OPEN, gate.getStatus(cacheName));
    }
}
