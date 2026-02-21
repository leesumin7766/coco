package com.example.shop.observability.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
public class IdempotencyKeyGenerator {

    private static final int MAX_LENGTH = 100;

    public String forTrade(Long productSizeId, Long buyBidId, Long sellBidId) {
        String businessKey = String.format("%d:%d:%d", productSizeId, buyBidId, sellBidId);
        return build("trade", businessKey);
    }

    public String forPayment(Long orderId, String paymentKey) {
        String normalizedPaymentKey = paymentKey == null || paymentKey.isBlank() ? "no-payment-key" : paymentKey;
        String businessKey = String.format("%d:%s", orderId, normalizedPaymentKey);
        return build("payment", businessKey);
    }

    public String forAudit(String action, String targetType, String targetId) {
        String businessKey = String.format("%s:%s:%s", action, targetType, targetId);
        return build("audit", businessKey);
    }

    private String build(String prefix, String businessKey) {
        String uuid = UUID.randomUUID().toString();
        String key = String.format("%s:%s:%s", prefix, businessKey, uuid);
        if (key.length() <= MAX_LENGTH) {
            return key;
        }
        String hash = sha256Hex(businessKey).substring(0, 16);
        return String.format("%s:%s:%s", prefix, hash, uuid);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
