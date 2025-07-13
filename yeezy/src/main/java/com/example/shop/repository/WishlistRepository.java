package com.example.shop.repository;

import com.example.shop.entity.WishlistEntity;
import com.example.shop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {
    List<WishlistEntity> findAllByUser(UserEntity user);
}
