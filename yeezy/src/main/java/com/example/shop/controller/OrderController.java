package com.example.shop.controller;

import com.example.shop.dto.OrderResponseDto;
import com.example.shop.entity.OrderEntity;
import com.example.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문 ID가 존재하지 않습니다: " + id));
        return ResponseEntity.ok(new OrderResponseDto(order));
    }
}
