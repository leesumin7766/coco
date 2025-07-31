package com.example.shop.service;

import com.example.shop.dto.PaymentResponseDto;
import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.UserEntity;
import com.example.shop.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponseDto preparePayment(Long orderId, String address, UserEntity user) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        // 본인 주문인지 체크
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 주문만 결제할 수 있습니다.");
        }

        // 상태 체크: PAYMENT_PENDING 상태에서만 결제 가능
        if (!order.getOrderStatus().getOrderStatus().equals("PAYMENT_PENDING")) {
            throw new IllegalStateException("이미 결제된 주문입니다.");
        }

        Long brandId = order.getBidding()
                .getProductSize()
                .getProduct()
                .getBrand()
                .getId();
        String formattedOrderId = "ORDER_" + (brandId == 10 ? 0 : brandId) + "_" + orderId;

        return new PaymentResponseDto(
                formattedOrderId,                           // Toss orderId
                order.getPrice(),                                    // 결제 금액
                order.getBidding().getProductSize().getProduct().getName(), // 상품명
                user.getEmail(),                                     // 사용자 이메일
                user.getName()                                       // 사용자 이름 (닉네임 대신)
        );
    }
}
