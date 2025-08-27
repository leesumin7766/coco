package com.example.shop.repository;

import com.example.shop.entity.WishlistEntity;
import com.example.shop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    // WishlistEntity에는 product 연관과 size(String)만 있음. product만 fetch하면 충분.
    @Query("""
        select w from WishlistEntity w
        join fetch w.product p
        where w.user = :user
        order by w.createdAt desc
        """)
    List<WishlistEntity> findAllByUser(@Param("user") UserEntity user);

}
