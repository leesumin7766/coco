package com.example.shop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key key;
    //블랙리스트 reids
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "bl:access:";

    private final long tokenValidityInMilliseconds = 1000 * 60 * 30; // 1시간

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public void blacklistToken(String token) {
        Date expiration = getExpiration(token);
        long ttlMs = expiration.getTime() - System.currentTimeMillis();
        System.out.println("[blacklistToken] ttlMs=" + ttlMs);

        if (ttlMs <= 0) {
            System.out.println("[blacklistToken] skipped (expired)");
            return;
        }

        String redisKey = BLACKLIST_PREFIX + token;

        redisTemplate.opsForValue().set(redisKey, "logout", ttlMs, TimeUnit.MILLISECONDS);

        Boolean exists = redisTemplate.hasKey(redisKey);
        String val = redisTemplate.opsForValue().get(redisKey);
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

        System.out.println("[blacklistToken] saved? exists=" + exists + ", val=" + val + ", ttl(s)=" + ttl);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
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