package com.example.shop.observability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_logs_actor_created_at", columnList = "actor_user_id,created_at"),
        @Index(name = "idx_audit_logs_target_created_at", columnList = "target_type,target_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "target_type", length = 60, nullable = false)
    private String targetType;

    @Column(name = "target_id", length = 100, nullable = false)
    private String targetId;

    @Lob
    @Column(name = "before_state")
    private String beforeState;

    @Lob
    @Column(name = "after_state")
    private String afterState;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "source_service", length = 60, nullable = false)
    private String sourceService;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
