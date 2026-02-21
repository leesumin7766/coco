package com.example.shop.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);

        jwtUtil = new JwtUtil(redisTemplate);
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "ThisIsASecretKeyForJwtTokenGenerationExample123!");
        jwtUtil.init();
    }

    @Test
    void isBlacklistedReturnsTrueWhenRedisFailsAndFailClosedEnabled() {
        ReflectionTestUtils.setField(jwtUtil, "jwtBlacklistFailClosed", true);
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));

        assertTrue(jwtUtil.isBlacklisted("access-token"));
    }

    @Test
    void isBlacklistedReturnsFalseWhenRedisFailsAndFailClosedDisabled() {
        ReflectionTestUtils.setField(jwtUtil, "jwtBlacklistFailClosed", false);
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));

        assertFalse(jwtUtil.isBlacklisted("access-token"));
    }

    @Test
    void blacklistTokenThrowsWhenRedisWriteFails() {
        String token = jwtUtil.createToken("test@example.com");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doThrow(new RuntimeException("redis write fail"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), Mockito.eq(TimeUnit.MILLISECONDS));

        assertThrows(IllegalStateException.class, () -> jwtUtil.blacklistToken(token));
    }
}
