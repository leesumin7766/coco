package com.example.shop.service;

import com.example.shop.client.TossClient;
import com.example.shop.dto.ConfirmPaymentRequestDto;
import com.example.shop.dto.TossResponseDto;
import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.OrderStatusEntity;
import com.example.shop.observability.service.ObservabilityService;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentService {

    private final TossClient tossClient;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ObservabilityService observabilityService;

    @Transactional
    public void confirmPayment(ConfirmPaymentRequestDto request) {
        // 1. Toss 서버에 결제 승인 요청
        TossResponseDto tossRes = tossClient.confirmPayment(request);

        // 2. 주문 조회
        String[] parts = request.getOrderId().split("_");
        Long orderId = Long.valueOf(parts[2]);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 3. 결제 금액 검증
        if (!order.getPrice().equals(request.getAmount())) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 4. 주문 상태 COMPLETED로 변경
        String previousStatus = order.getOrderStatus().getOrderStatus();
        OrderStatusEntity completedStatus = orderStatusRepository.findByOrderStatus("COMPLETED")
                .orElseThrow(() -> new IllegalStateException("COMPLETED 상태가 없습니다."));
        order.setOrderStatus(completedStatus);

        // 5. 결제 승인 시간 저장
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        Map<String, Object> paymentPayload = new HashMap<>();
        paymentPayload.put("orderId", request.getOrderId());
        paymentPayload.put("tossOrderId", tossRes != null ? tossRes.getOrderId() : null);

        observabilityService.savePaymentEvent(
                order.getId(),
                request.getPaymentKey(),
                "PAYMENT_CONFIRM",
                "CONFIRMED",
                request.getAmount(),
                paymentPayload
        );

        observabilityService.saveAuditLog(
                order.getBuyer() != null ? order.getBuyer().getId() : null,
                "PAYMENT_CONFIRMED",
                "ORDER",
                String.valueOf(order.getId()),
                Map.of("orderStatus", previousStatus),
                Map.of("orderStatus", completedStatus.getOrderStatus())
        );
    }
}
