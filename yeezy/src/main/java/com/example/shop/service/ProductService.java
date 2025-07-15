package com.example.shop.service;

import com.example.shop.dto.ProductDetailResponseDto;
import com.example.shop.dto.ProductRequestDto;
import com.example.shop.dto.ProductResponseDto;
import com.example.shop.dto.ProductSearchResponseDto;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductEntity registerProduct(ProductRequestDto dto, Long userId) {
        BrandEntity brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("브랜드가 존재하지 않습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        ProductEntity product = new ProductEntity();
        product.setBrand(brand);
        product.setName(dto.getName());
        product.setNameKr(dto.getNameKr());
        product.setModelNumber(dto.getModelNumber());
        product.setReleasePrice(dto.getReleasePrice());
        product.setCreatedAt(LocalDateTime.now());

        // 사이즈 등록
        for (Integer sizeId : dto.getSizeIds()) {
            SizeEntity size = sizeRepository.findById(sizeId.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("사이즈가 존재하지 않습니다."));

            ProductSizeEntity productSize = new ProductSizeEntity();
            productSize.setProduct(product);
            productSize.setSize(size);

            product.getProductSizes().add(productSize); // 양방향 관계일 경우 필요
        }

        // 이미지 등록
        for (String imageUrl : dto.getImageUrls()) {
            ProductImageEntity image = new ProductImageEntity();
            image.setProduct(product);
            image.setImageUrl(imageUrl);

            product.getProductImages().add(image); // 양방향 관계일 경우 필요
        }
        product.setUser(user);
        System.out.println("등록된 사용자 ID: " + product.getUser().getId());
        return productRepository.save(product);
    }

    public List<ProductResponseDto> getProductsByUserId(Long userId) {
        List<ProductEntity> products = productRepository.findByUserId(userId);
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .toList();
    }

    public List<ProductSearchResponseDto> searchProductsByNameOrModel(String query) {
        // repository에서 LIKE 검색 실행
        return productRepository.searchByNameOrModel(query);
    }

    public ProductDetailResponseDto getProductDetail(Long productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
        return ProductDetailResponseDto.fromEntity(product);
    }

}
