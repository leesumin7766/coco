package com.example.shop.repository;

import com.example.shop.entity.TradeEntity;
import com.example.shop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    List<TradeEntity> findByBuyerOrSeller(UserEntity buyer, UserEntity seller);
}
