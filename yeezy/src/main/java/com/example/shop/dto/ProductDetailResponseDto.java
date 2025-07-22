package com.example.shop.dto;

import com.example.shop.entity.ProductEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductDetailResponseDto {
    private Long id;
    private String name;
    private String nameKr;
    private String modelNumber;
    private String brandName;
    private int releasePrice;
    private LocalDateTime createdAt;

    private List<String> imageUrls;
    private List<String> sizes;

    public static ProductDetailResponseDto fromEntity(ProductEntity product) {
        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .nameKr(product.getNameKr())
                .modelNumber(product.getModelNumber())
                .brandName(product.getBrand().getName())
                .releasePrice(product.getReleasePrice())
                .createdAt(product.getCreatedAt())
                .imageUrls(
                        product.getProductImages().stream()
                                .map(img -> img.getImageUrl())
                                .collect(Collectors.toList())
                )
                .sizes(
                        product.getProductSizes().stream()
                                .map(ps -> ps.getSize().getName())
                                .collect(Collectors.toList())
                )
                .build();
    }
}