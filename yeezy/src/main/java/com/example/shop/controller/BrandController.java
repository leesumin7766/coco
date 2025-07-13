package com.example.shop.controller;

import com.example.shop.entity.BrandEntity;
import com.example.shop.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    public List<BrandEntity> getAllBrands() {
        return brandRepository.findAll();
    }
}
