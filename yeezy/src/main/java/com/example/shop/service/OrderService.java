package com.example.shop.service;

import com.example.shop.entity.BiddingEntity;
import com.example.shop.entity.OrderEntity;
import com.example.shop.entity.OrderStatusEntity;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

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
