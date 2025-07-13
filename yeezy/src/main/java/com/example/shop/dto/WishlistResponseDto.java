package com.example.shop.dto;

import com.example.shop.entity.WishlistEntity;
import lombok.Getter;

@Getter
public class WishlistResponseDto {
    private Long id;
    private String productName;
    private int price;

    public WishlistResponseDto(WishlistEntity wishlist) {
        this.id = wishlist.getId();
        this.productName = wishlist.getProductName();
        this.price = wishlist.getPrice();
    }
}
