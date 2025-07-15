package com.example.shop.controller;

import com.example.shop.dto.ProductDetailResponseDto;
import com.example.shop.dto.ProductRequestDto;
import com.example.shop.dto.ProductSearchResponseDto;
import com.example.shop.entity.ProductEntity;
import com.example.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.shop.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor

public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> registerProduct(@RequestBody ProductRequestDto dto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        ProductEntity savedProduct = productService.registerProduct(dto, userId);
        return ResponseEntity.ok(savedProduct.getId());
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam("query") String query) {
        return ResponseEntity.ok(productService.searchProductsByNameOrModel(query));
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponseDto dto = productService.getProductDetail(productId);
        return ResponseEntity.ok(dto);
    }
}

