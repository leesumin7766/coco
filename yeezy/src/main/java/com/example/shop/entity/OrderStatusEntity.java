package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_status")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_status", nullable = false, unique = true)
    private String orderStatus;

    public OrderStatusEntity(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}