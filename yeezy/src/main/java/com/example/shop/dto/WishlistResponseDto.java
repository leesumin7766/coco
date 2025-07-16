package com.example.shop.dto;

import com.example.shop.entity.WishlistEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WishlistResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productNameKr;
    private String modelNumber;
    private String imageUrl;
    private int releasePrice;

    public WishlistResponseDto(WishlistEntity entity) {
        this.id = entity.getId();
        this.productId = entity.getProduct().getId();
        this.productName = entity.getProduct().getName();
        this.productNameKr = entity.getProduct().getNameKr();
        this.modelNumber = entity.getProduct().getModelNumber();
        this.releasePrice = entity.getProduct().getReleasePrice();
        this.imageUrl = entity.getProduct().getProductImages().stream()
                .map(img -> img.getImageUrl())
                .findFirst()
                .orElse(null);
    }
}

