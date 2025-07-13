package com.example.shop.service;

import com.example.shop.dto.OrderResponseDto;
import com.example.shop.dto.UserResponseDto;
import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.UserEntity;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.UserRepository;
import jakarta.persistence.criteria.Order;
import lombok.RequiredArgsConstructor;
import com.example.shop.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<OrderResponseDto> getOrderHistory(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<OrderEntity> orders = orderRepository.findAllByBuyer(user);  // 수정됨
        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList());
    }

    public UserResponseDto getUserInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserResponseDto(user);  // → DTO 필요
    }

    // service/MypageService.java
    public void changePassword(Long userId, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

}
