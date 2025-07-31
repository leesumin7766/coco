package com.example.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossResponseDto {
    private String orderId;
    private String paymentKey;
    private String status;
    private String method;
    private String approvedAt;
}
