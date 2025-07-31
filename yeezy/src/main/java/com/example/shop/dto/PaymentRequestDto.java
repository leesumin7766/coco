package com.example.shop.dto;

import lombok.Getter;

@Getter
public class PaymentRequestDto {
    private Long orderId;
    private String address;
}