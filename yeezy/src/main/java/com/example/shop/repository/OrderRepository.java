package com.example.shop.repository;

import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByBuyer(UserEntity buyer);
}
