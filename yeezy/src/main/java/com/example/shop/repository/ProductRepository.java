package com.example.shop.repository;

import com.example.shop.dto.ProductSearchResponseDto;
import com.example.shop.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByUserId(Long userId);

    // HQL JPA 쿼리
    @Query("SELECT new com.example.shop.dto.ProductSearchResponseDto(" +
            "p.id, p.name, p.nameKr, p.modelNumber, pi.imageUrl, p.releasePrice) " + // ← nameKr 추가됨
            "FROM ProductEntity p " +
            "LEFT JOIN ProductImageEntity pi ON pi.product.id = p.id " +
            "WHERE p.name LIKE %:query% OR p.nameKr LIKE %:query% OR p.modelNumber LIKE %:query%")
    List<ProductSearchResponseDto> searchByNameOrModel(@Param("query") String query);
}
