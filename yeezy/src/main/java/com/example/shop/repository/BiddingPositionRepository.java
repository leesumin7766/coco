package com.example.shop.repository;

import com.example.shop.entity.BiddingPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiddingPositionRepository extends JpaRepository<BiddingPositionEntity, Long> {
    Optional<BiddingPositionEntity> findByPosition(String position);  // ex: "SELL"
}

