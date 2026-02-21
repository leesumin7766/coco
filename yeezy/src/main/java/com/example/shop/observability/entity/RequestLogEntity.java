package com.example.shop.observability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs", indexes = {
        @Index(name = "idx_request_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_request_logs_path_created_at", columnList = "path,created_at"),
        @Index(name = "idx_request_logs_status_created_at", columnList = "status_code,created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class RequestLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", length = 64, nullable = false)
    private String traceId;

    @Column(name = "method", length = 10, nullable = false)
    private String method;

    @Column(name = "path", length = 255, nullable = false)
    private String path;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "latency_ms", nullable = false)
    private Long latencyMs;

    @Column(name = "db_time_ms")
    private Long dbTimeMs;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
