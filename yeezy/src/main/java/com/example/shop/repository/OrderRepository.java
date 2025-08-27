package com.example.shop.repository;

import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 마이페이지 주문목록에서도 Lazy 문제 없이 쓰도록 같은 fetch plan 적용 (메서드명은 그대로 유지)
    @Query("""
        select distinct o from OrderEntity o
        join fetch o.bidding b
        join fetch b.productSize ps
        join fetch ps.product p
        join fetch ps.size sz
        left join fetch o.orderStatus os
        left join fetch o.seller seller
        where o.buyer = :buyer
        order by o.createdAt desc
        """)
    List<OrderEntity> findAllByBuyer(@Param("buyer") UserEntity buyer);

    // 주문 상세 조회 (size 별칭은 sz, 판매자 별칭은 seller로 명확히)
    @Query("""
        select o from OrderEntity o
        join fetch o.bidding b
        join fetch b.productSize ps
        join fetch ps.product p
        join fetch ps.size sz
        left join fetch o.orderStatus os
        left join fetch o.seller seller
        where o.id = :id
        """)
    Optional<OrderEntity> findDetailById(@Param("id") Long id);
//    // 결제용 비즈니스 키 조회 (기존 로직 유지)
//    Optional<OrderEntity> findByOrderId(String orderId);
}
