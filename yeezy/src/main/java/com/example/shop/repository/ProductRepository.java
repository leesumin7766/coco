package com.example.shop.repository;

import com.example.shop.dto.ProductSearchResponseDto;
import com.example.shop.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByUserId(Long userId);
/*
    // HQL JPA 쿼리
    @Query("SELECT new com.example.shop.dto.ProductSearchResponseDto(" +
            "p.id, p.name, p.nameKr, p.modelNumber, pi.imageUrl, p.releasePrice) " + // ← nameKr 추가됨
            "FROM ProductEntity p " +
            "LEFT JOIN ProductImageEntity pi ON pi.product.id = p.id " +
            "WHERE p.name LIKE %:query% OR p.nameKr LIKE %:query% OR p.modelNumber LIKE %:query%")
    List<ProductSearchResponseDto> searchByNameOrModel(@Param("query") String query);
*/
    // (기존) 검색용 DTO 프로젝션 — 그대로 사용 가능 (LAZY 문제 없음)
    @Query("""
           SELECT new com.example.shop.dto.ProductSearchResponseDto(
               p.id, p.name, p.nameKr, p.modelNumber, pi.imageUrl, p.releasePrice
           )
           FROM ProductEntity p
           LEFT JOIN ProductImageEntity pi ON pi.product.id = p.id
           WHERE p.name LIKE %:query% OR p.nameKr LIKE %:query% OR p.modelNumber LIKE %:query%
           """)
    List<ProductSearchResponseDto> searchByNameOrModel(@Param("query") String query);

    // ✅ 마이페이지 전용: 브랜드/이미지까지 한 번에 로딩 (중복 제거를 위해 DISTINCT)
    @Query("""
           SELECT DISTINCT p
           FROM ProductEntity p
           LEFT JOIN FETCH p.brand
           LEFT JOIN FETCH p.productImages
           WHERE p.user.id = :userId
             AND p.deletedAt IS NULL
           """)
    List<ProductEntity> findAllByUserIdWithImages(@Param("userId") Long userId);

    // ✅ 상세 페이지 전용: 브랜드/이미지/사이즈까지 모두 로딩
    @Query("""
           SELECT DISTINCT p
           FROM ProductEntity p
           LEFT JOIN FETCH p.brand
           LEFT JOIN FETCH p.productImages
           WHERE p.id = :id
           """)
    Optional<ProductEntity> findDetailById(@Param("id") Long id);
}

