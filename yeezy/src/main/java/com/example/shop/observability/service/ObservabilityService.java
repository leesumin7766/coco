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

    private final RequestLogRepository requestLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final TradeEventRepository tradeEventRepository;
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
                             Object afterState) {
        AuditLogEntity log = new AuditLogEntity();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeState(toJson(beforeState));
        log.setAfterState(toJson(afterState));
        log.setTraceId(TraceContext.getTraceId());
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void savePaymentEvent(Long orderId,
                                 String paymentKey,
                                 String eventType,
                                 String status,
                                 Integer amount,
                                 Map<String, Object> payload) {
        PaymentEventEntity event = new PaymentEventEntity();
        event.setOrderId(orderId);
        event.setPaymentKey(paymentKey);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setAmount(amount);
        event.setProvider("TOSS");
        event.setTraceId(TraceContext.getTraceId());
        event.setPayload(toJson(payload));
        event.setCreatedAt(LocalDateTime.now());
        paymentEventRepository.save(event);
    }

    public void saveTradeEvent(Long orderId,
                               Long buyBiddingId,
                               Long sellBiddingId,
                               Long productSizeId,
                               Integer price,
                               String eventType,
                               String status,
                               Map<String, Object> payload) {
        TradeEventEntity event = new TradeEventEntity();
        event.setOrderId(orderId);
        event.setBuyBiddingId(buyBiddingId);
        event.setSellBiddingId(sellBiddingId);
        event.setProductSizeId(productSizeId);
        event.setPrice(price);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setTraceId(TraceContext.getTraceId());
        event.setPayload(toJson(payload));
        event.setCreatedAt(LocalDateTime.now());
        tradeEventRepository.save(event);
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
