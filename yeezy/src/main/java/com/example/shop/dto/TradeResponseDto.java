package com.example.shop.dto;

import com.example.shop.entity.TradeEntity;
import com.example.shop.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TradeResponseDto {
    private String productName;
    private String size;
    private int price;
    private String role; // BUYER or SELLER
    private LocalDateTime tradedAt;

    public static TradeResponseDto fromEntity(TradeEntity trade, UserEntity currentUser) {
        return new TradeResponseDto(
                trade.getProduct().getName(),
                trade.getSize(),
                trade.getPrice(),
                trade.getBuyer().equals(currentUser) ? "BUYER" : "SELLER",
                trade.getTradedAt()
        );
    }
}
