package com.example.shop.dto;

import lombok.Getter;

@Getter
public class BiddingRequestDto {
    private Long productId;
    private String size;
    private int price;
    private String position; // "SELL" 또는 "BUY"
}
