package com.example.shop.repository;

import com.example.shop.entity.BiddingEntity;
import com.example.shop.entity.UserEntity;
import com.example.shop.entity.ProductSizeEntity;
import com.example.shop.entity.BiddingPositionEntity;
import com.example.shop.entity.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BiddingRepository extends JpaRepository<BiddingEntity, Long> {

    // 예: 특정 유저의 SELL 입찰
    List<BiddingEntity> findByUserAndPosition(UserEntity user, BiddingPositionEntity position);

    List<BiddingEntity> findByUserAndPositionAndStatus(UserEntity user, BiddingPositionEntity position, StatusEntity status);

    // 예: 특정 상품/사이즈/포지션의 가장 높은 가격
    List<BiddingEntity> findByProductSizeAndPositionAndStatusOrderByPriceDesc(
            ProductSizeEntity productSize,
            BiddingPositionEntity position,
            StatusEntity status
    );

    Optional<BiddingEntity> findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceAsc(
            ProductSizeEntity productSize,
            String position,
            StatusEntity status
    );

    Optional<BiddingEntity> findTopByProductSizeAndPosition_PositionAndStatusOrderByPriceDesc(
            ProductSizeEntity productSize,
            String position,
            StatusEntity status
    );
}
