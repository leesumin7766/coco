package com.example.shop.dto;

import com.example.shop.entity.ProductEntity;
import com.example.shop.entity.ProductImageEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String nameKr;
    private String modelNumber;
    private int releasePrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private List<String> imageUrls;
    public static ProductResponseDto fromEntity(ProductEntity product) {
        List<String> urls = product.getProductImages().stream()
                .map(ProductImageEntity::getImageUrl)
                .toList();

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getNameKr(),
                product.getModelNumber(),
                product.getReleasePrice(),
                product.getCreatedAt(),
                urls
        );
    }
}
