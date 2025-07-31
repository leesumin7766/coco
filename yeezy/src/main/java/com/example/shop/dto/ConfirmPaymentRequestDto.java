package com.example.shop.dto;

import lombok.Getter;

@Getter
public class ConfirmPaymentRequestDto {
    private String paymentKey;
    private String orderId;
    private int amount;
}
