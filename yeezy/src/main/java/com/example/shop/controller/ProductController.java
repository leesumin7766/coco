package com.example.shop.controller;

import com.example.shop.dto.ProductRequestDto;
import com.example.shop.entity.ProductEntity;
import com.example.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> registerProduct(@RequestBody ProductRequestDto dto) {
        ProductEntity savedProduct = productService.registerProduct(dto);
        return ResponseEntity.ok(savedProduct.getId());
    }
}

