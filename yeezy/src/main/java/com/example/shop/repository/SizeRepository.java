package com.example.shop.repository;

import com.example.shop.entity.SizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SizeRepository extends JpaRepository<SizeEntity, Long> {
    Optional<SizeEntity> findByName(String name);
}

