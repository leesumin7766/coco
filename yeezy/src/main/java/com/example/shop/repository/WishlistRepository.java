package com.example.shop.repository;

import com.example.shop.entity.WishlistEntity;
import com.example.shop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

//    @Query("""
//        select distinct w
//        from WishlistEntity w
//        join fetch w.product p
//        left join fetch p.productImages pi
//        where w.user = :user
//        """)
    List<WishlistEntity> findAllByUser(@Param("user") UserEntity user);

}
