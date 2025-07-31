package com.example.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponseDto {
    private String orderId;      // Toss에 넘길 orderId (우리 시스템의 orderId 사용)
    private int amount;          // 금액 (order.getPrice())
    private String orderName;    // 상품명
    private String customerEmail;
    private String customerName;
}
