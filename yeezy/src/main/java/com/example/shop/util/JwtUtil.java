package com.example.shop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key key;
    //블랙리스트 reids
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "bl:access:";

    private final long tokenValidityInMilliseconds = 1000 * 60 * 30; // 1시간

    @Value("${redis.resilience.jwt-blacklist-fail-closed:true}")
    private boolean jwtBlacklistFailClosed;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public void blacklistToken(String token) {
        Date expiration = getExpiration(token);
        long ttlMs = expiration.getTime() - System.currentTimeMillis();

        if (ttlMs <= 0) {
            log.debug("skip blacklisting expired token");
            return;
        }

        String redisKey = BLACKLIST_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(redisKey, "logout", ttlMs, TimeUnit.MILLISECONDS);
            log.debug("token blacklisted. key={}, ttlMs={}", redisKey, ttlMs);
        } catch (RuntimeException e) {
            log.warn("failed to write JWT blacklist in Redis. key={}, reason={}", redisKey, e.getMessage());
            throw new IllegalStateException("Redis blacklist write failed", e);
        }
    }

    public boolean isBlacklisted(String token) {
        String redisKey = BLACKLIST_PREFIX + token;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (RuntimeException e) {
            if (jwtBlacklistFailClosed) {
                log.warn("Redis unavailable during blacklist check. fail-closed=true, key={}, reason={}",
                        redisKey, e.getMessage());
                return true;
            }
            log.warn("Redis unavailable during blacklist check. fail-closed=false, key={}, reason={}",
                    redisKey, e.getMessage());
            return false;
        }
    }

    public String createToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            if (isBlacklisted(token)) return false;
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private Date getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}
