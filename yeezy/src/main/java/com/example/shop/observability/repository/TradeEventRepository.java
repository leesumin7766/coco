package com.example.shop.observability.repository;

import com.example.shop.observability.entity.TradeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeEventRepository extends JpaRepository<TradeEventEntity, Long> {
}
