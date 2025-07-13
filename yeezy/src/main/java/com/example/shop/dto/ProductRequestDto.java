package com.example.shop.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductRequestDto {
    private Long brandId;
    private String nameKr;
    private String name;
    private String modelNumber;
    private int releasePrice;
    private List<Integer> sizeIds;
    private List<String> imageUrls;
}
