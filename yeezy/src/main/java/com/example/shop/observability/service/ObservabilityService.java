package com.example.shop.observability.service;

import com.example.shop.observability.entity.AuditLogEntity;
import com.example.shop.observability.entity.PaymentEventEntity;
import com.example.shop.observability.entity.RequestLogEntity;
import com.example.shop.observability.entity.TradeEventEntity;
import com.example.shop.observability.repository.AuditLogRepository;
import com.example.shop.observability.repository.PaymentEventRepository;
import com.example.shop.observability.repository.RequestLogRepository;
import com.example.shop.observability.repository.TradeEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ObservabilityService {

    private static final String SOURCE_SERVICE = "backend";
    private static final int EVENT_VERSION = 1;

    private final RequestLogRepository requestLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final TradeEventRepository tradeEventRepository;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRequestLog(String traceId,
                               String method,
                               String path,
                               int statusCode,
                               long latencyMs,
                               Long userId,
                               String clientIp) {
        RequestLogEntity log = new RequestLogEntity();
        log.setTraceId(traceId);
        log.setMethod(method);
        log.setPath(path);
        log.setStatusCode(statusCode);
        log.setLatencyMs(latencyMs);
        log.setUserId(userId);
        log.setClientIp(clientIp);
        log.setCreatedAt(LocalDateTime.now());
        requestLogRepository.save(log);
    }

    public void saveAuditLog(Long actorUserId,
                             String action,
                             String targetType,
                             String targetId,
                             Object beforeState,
                             Object afterState,
                             String idempotencyKey) {
        LocalDateTime now = LocalDateTime.now();
        AuditLogEntity log = new AuditLogEntity();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeState(toJson(beforeState));
        log.setAfterState(toJson(afterState));
        log.setTraceId(TraceContext.getTraceId());
        log.setIdempotencyKey(resolveAuditIdempotencyKey(action, targetType, targetId, idempotencyKey));
        log.setSourceService(SOURCE_SERVICE);
        log.setEventVersion(EVENT_VERSION);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        auditLogRepository.save(log);
    }

    public void savePaymentEvent(Long orderId,
                                 String paymentKey,
                                 String eventType,
                                 String status,
                                 String prevStatus,
                                 String newStatus,
                                 Integer amount,
                                 Long actorUserId,
                                 String idempotencyKey,
                                 Map<String, Object> payload) {
        LocalDateTime now = LocalDateTime.now();
        PaymentEventEntity event = new PaymentEventEntity();
        event.setOrderId(orderId);
        event.setPaymentKey(paymentKey);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setPrevStatus(prevStatus);
        event.setNewStatus(newStatus);
        event.setAmount(amount);
        event.setProvider("TOSS");
        event.setTraceId(TraceContext.getTraceId());
        event.setIdempotencyKey(resolvePaymentIdempotencyKey(orderId, paymentKey, idempotencyKey));
        event.setEventVersion(EVENT_VERSION);
        event.setSourceService(SOURCE_SERVICE);
        event.setActorUserId(actorUserId);
        event.setPayload(toJson(payload));
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        paymentEventRepository.save(event);
    }

    public void saveTradeEvent(Long orderId,
                               Long buyBiddingId,
                               Long sellBiddingId,
                               Long productSizeId,
                               Integer price,
                               String eventType,
                               String status,
                               String prevStatus,
                               String newStatus,
                               Long actorUserId,
                               String idempotencyKey,
                               Map<String, Object> payload) {
        LocalDateTime now = LocalDateTime.now();
        TradeEventEntity event = new TradeEventEntity();
        event.setOrderId(orderId);
        event.setBuyBiddingId(buyBiddingId);
        event.setSellBiddingId(sellBiddingId);
        event.setProductSizeId(productSizeId);
        event.setPrice(price);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setPrevStatus(prevStatus);
        event.setNewStatus(newStatus);
        event.setTraceId(TraceContext.getTraceId());
        event.setIdempotencyKey(resolveTradeIdempotencyKey(productSizeId, buyBiddingId, sellBiddingId, idempotencyKey));
        event.setEventVersion(EVENT_VERSION);
        event.setSourceService(SOURCE_SERVICE);
        event.setActorUserId(actorUserId);
        event.setPayload(toJson(payload));
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        tradeEventRepository.save(event);
    }

    private String resolveAuditIdempotencyKey(String action,
                                              String targetType,
                                              String targetId,
                                              String explicitIdempotencyKey) {
        if (explicitIdempotencyKey != null && !explicitIdempotencyKey.isBlank()) {
            return explicitIdempotencyKey;
        }
        return idempotencyKeyGenerator.forAudit(action, targetType, targetId);
    }

    private String resolvePaymentIdempotencyKey(Long orderId,
                                                String paymentKey,
                                                String explicitIdempotencyKey) {
        if (explicitIdempotencyKey != null && !explicitIdempotencyKey.isBlank()) {
            return explicitIdempotencyKey;
        }
        return idempotencyKeyGenerator.forPayment(orderId, paymentKey);
    }

    private String resolveTradeIdempotencyKey(Long productSizeId,
                                              Long buyBiddingId,
                                              Long sellBiddingId,
                                              String explicitIdempotencyKey) {
        if (explicitIdempotencyKey != null && !explicitIdempotencyKey.isBlank()) {
            return explicitIdempotencyKey;
        }
        return idempotencyKeyGenerator.forTrade(productSizeId, buyBiddingId, sellBiddingId);
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }
}
