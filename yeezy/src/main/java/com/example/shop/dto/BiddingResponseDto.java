package com.example.shop.dto;

import com.example.shop.entity.BiddingEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BiddingResponseDto {

    private Long id;                    // 입찰 ID
    private String productName;        // 상품명
    private String size;               // 사이즈
    private int price;                  // 등록가
    private String position;
    private String status;             // 상태 (예: ACTIVE, COMPLETED)
    private LocalDateTime createdAt;   // 등록일

    public static BiddingResponseDto fromEntity(BiddingEntity bidding) {
        return BiddingResponseDto.builder()
                .id(bidding.getId())
                .productName(bidding.getProductSize().getProduct().getNameKr())
                .size(bidding.getProductSize().getSize().getName()) // String으로 설정한 경우
                .price(bidding.getPrice())
                .position(bidding.getPosition().getPosition())
                .status(bidding.getStatus().getName()) // "ACTIVE" 등 문자열
                .createdAt(bidding.getCreatedAt())
                .build();
    }
}