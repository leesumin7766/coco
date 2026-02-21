package com.example.shop.observability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_events", indexes = {
        @Index(name = "idx_payment_events_order_created_at", columnList = "order_id,created_at"),
        @Index(name = "idx_payment_events_status_created_at", columnList = "status,created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "provider", length = 30)
    private String provider;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
