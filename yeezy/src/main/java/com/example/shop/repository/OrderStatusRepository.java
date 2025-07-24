package com.example.shop.repository;

import com.example.shop.entity.OrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatusEntity, Long> {
    Optional<OrderStatusEntity> findByOrderStatus(String orderStatus);
}