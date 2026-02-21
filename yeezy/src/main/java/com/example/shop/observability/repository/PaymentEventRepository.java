package com.example.shop.observability.repository;

import com.example.shop.observability.entity.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity, Long> {
}
