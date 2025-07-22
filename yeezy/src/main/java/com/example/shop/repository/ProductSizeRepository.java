package com.example.shop.repository;


import com.example.shop.entity.ProductEntity;
import com.example.shop.entity.ProductSizeEntity;
import com.example.shop.entity.SizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<ProductSizeEntity, Long> {
    Optional<ProductSizeEntity> findByProductAndSize(ProductEntity product, SizeEntity size);
}