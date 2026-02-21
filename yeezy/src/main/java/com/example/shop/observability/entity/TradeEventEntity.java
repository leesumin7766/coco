package com.example.shop.observability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_events", indexes = {
        @Index(name = "idx_trade_events_order_created_at", columnList = "order_id,created_at"),
        @Index(name = "idx_trade_events_status_created_at", columnList = "status,created_at"),
        @Index(name = "idx_trade_events_trace_created_at", columnList = "trace_id,created_at")
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

    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String idempotencyKey;

    @Column(name = "prev_status", length = 50)
    private String prevStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "source_service", length = 60, nullable = false)
    private String sourceService;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
