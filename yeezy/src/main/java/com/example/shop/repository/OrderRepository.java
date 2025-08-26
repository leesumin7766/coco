package com.example.shop.repository;

import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByBuyer(UserEntity buyer);

    @Query("""
        select o from OrderEntity o
        join fetch o.bidding b
        join fetch b.productSize ps
        join fetch ps.product p
        left join fetch o.orderStatus os
        left join fetch o.seller s
        where o.id = :id
        """)
    Optional<OrderEntity> findDetailById(@Param("id") Long id);

//    // 결제용 비즈니스 키 조회 (기존 로직 유지)
//    Optional<OrderEntity> findByOrderId(String orderId);
}
