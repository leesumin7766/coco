package com.example.shop.controller;

import com.example.shop.dto.OrderResponseDto;
import com.example.shop.entity.OrderEntity;
import com.example.shop.repository.OrderRepository;
import com.example.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        // 서비스 계층에서 fetch join 으로 안전하게 로딩
        OrderEntity order = orderService.getOrderDetail(id);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }
}
