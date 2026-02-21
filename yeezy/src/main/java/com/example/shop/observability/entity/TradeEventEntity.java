package com.example.shop.observability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_events", indexes = {
        @Index(name = "idx_trade_events_order_created_at", columnList = "order_id,created_at"),
        @Index(name = "idx_trade_events_status_created_at", columnList = "status,created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class TradeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "buy_bidding_id")
    private Long buyBiddingId;

    @Column(name = "sell_bidding_id")
    private Long sellBiddingId;

    @Column(name = "product_size_id")
    private Long productSizeId;

    @Column(name = "price")
    private Integer price;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
