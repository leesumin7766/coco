package com.example.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSearchResponseDto {
    private Long id;
    private String name;
    private String nameKr;
    private String modelNumber;
    private String imageUrl;
    private int releasePrice;
}
