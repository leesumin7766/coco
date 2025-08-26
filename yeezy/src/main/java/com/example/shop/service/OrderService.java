package com.example.shop.service;

import com.example.shop.entity.BiddingEntity;
import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.OrderStatusEntity;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderEntity getOrderDetail(Long id) {
        return orderRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public OrderEntity createOrder(BiddingEntity matchedBidding, BiddingEntity newBidding) {
        OrderStatusEntity pendingStatus = orderStatusRepository.findByOrderStatus("PAYMENT_PENDING")
                .orElseThrow(() -> new IllegalArgumentException("PAYMENT_PENDING 상태 없음"));

        OrderEntity order = new OrderEntity();
        order.setOrderStatus(pendingStatus);
        order.setBidding(newBidding.getPosition().getPosition().equals("BUY") ? newBidding : matchedBidding);
        order.setBuyer(newBidding.getPosition().getPosition().equals("BUY") ? newBidding.getUser() : matchedBidding.getUser());
        order.setSeller(newBidding.getPosition().getPosition().equals("BUY") ? matchedBidding.getUser() : newBidding.getUser());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
