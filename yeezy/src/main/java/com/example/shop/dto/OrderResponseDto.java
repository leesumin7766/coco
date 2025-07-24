package com.example.shop.dto;

import com.example.shop.entity.OrderEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderResponseDto {

    private String productName;  // 상품명은 ProductEntity에서 가져옴
    private int quantity;        // quantity는 없으므로 생략하거나 다른 곳에서 가져와야 함
    private int totalPrice;      // 가격은 BiddingEntity.price로 대체 가능
    private LocalDateTime orderDate;
    private String orderStatus;  // 주문 상태명 추가

    private String size;     // 추가
    private String seller;   // 추가
    private Integer price;

    public OrderResponseDto(OrderEntity order) {
        // 상품명: order -> bidding -> productSize -> product -> name
        this.productName = order.getBidding()
                .getProductSize()
                .getProduct()
                .getName();

        // 수량은 테이블에 없으므로 1로 기본 설정하거나 삭제
        this.quantity = 1;

        // 가격: 입찰 가격
        this.totalPrice = order.getBidding().getPrice();

        // 주문 생성일
        this.orderDate = order.getCreatedAt();

        // 주문 상태명
        this.orderStatus = order.getOrderStatus().getOrderStatus();

        this.price = order.getPrice();
        this.size = order.getProductSize() != null && order.getProductSize().getSize() != null
                ? order.getProductSize().getSize().getName()
                : null;

        // 판매자
        this.seller = order.getSeller() != null
                ? order.getSeller().getEmail() // 또는 getName(), getNickname()
                : null;
    }
}
