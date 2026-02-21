-- 컨테이너 재기동 시에도 안전하게 반복 적용되는 보강 스크립트
CREATE DATABASE IF NOT EXISTS yeezydb;
USE yeezydb;

CREATE TABLE IF NOT EXISTS request_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trace_id VARCHAR(64) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(255) NOT NULL,
    status_code INT NOT NULL,
    latency_ms BIGINT NOT NULL,
    db_time_ms BIGINT NULL,
    user_id BIGINT NULL,
    client_ip VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_request_logs_created_at (created_at),
    KEY idx_request_logs_path_created_at (path, created_at),
    KEY idx_request_logs_status_created_at (status_code, created_at)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    before_state LONGTEXT NULL,
    after_state LONGTEXT NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_logs_created_at (created_at),
    KEY idx_audit_logs_actor_created_at (actor_user_id, created_at)
);

CREATE TABLE IF NOT EXISTS payment_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payment_key VARCHAR(200) NULL,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount INT NULL,
    provider VARCHAR(30) NULL,
    trace_id VARCHAR(64) NULL,
    payload LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_payment_events_order_created_at (order_id, created_at),
    KEY idx_payment_events_status_created_at (status, created_at)
);

CREATE TABLE IF NOT EXISTS trade_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NULL,
    buy_bidding_id BIGINT NULL,
    sell_bidding_id BIGINT NULL,
    product_size_id BIGINT NULL,
    price INT NULL,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    trace_id VARCHAR(64) NULL,
    payload LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_trade_events_order_created_at (order_id, created_at),
    KEY idx_trade_events_status_created_at (status, created_at)
);

CREATE TABLE IF NOT EXISTS metrics_hourly (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bucket_start DATETIME NOT NULL,
    metric_name VARCHAR(80) NOT NULL,
    dimension_key VARCHAR(120) NOT NULL,
    metric_value BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_metrics_hourly (bucket_start, metric_name, dimension_key),
    KEY idx_metrics_hourly_metric_time (metric_name, bucket_start)
);

CREATE TABLE IF NOT EXISTS metrics_daily (
    id BIGINT NOT NULL AUTO_INCREMENT,
    metric_date DATE NOT NULL,
    metric_name VARCHAR(80) NOT NULL,
    dimension_key VARCHAR(120) NOT NULL,
    metric_value BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_metrics_daily (metric_date, metric_name, dimension_key),
    KEY idx_metrics_daily_metric_date (metric_name, metric_date)
);

CREATE TABLE IF NOT EXISTS data_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    data_domain VARCHAR(60) NOT NULL,
    is_pii BOOLEAN NOT NULL DEFAULT FALSE,
    pii_type VARCHAR(40) NULL,
    masking_rule VARCHAR(80) NULL,
    retention_days INT NULL,
    owner_team VARCHAR(80) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_catalog (table_name, column_name)
);

CREATE TABLE IF NOT EXISTS batch_job_meta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_name VARCHAR(120) NOT NULL,
    schedule_desc VARCHAR(120) NULL,
    last_run_at DATETIME NULL,
    last_success_at DATETIME NULL,
    last_status VARCHAR(30) NULL,
    fail_count INT NOT NULL DEFAULT 0,
    sla_seconds INT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_job_meta_job_name (job_name)
);

INSERT IGNORE INTO data_catalog (
    table_name, column_name, data_domain, is_pii, pii_type, masking_rule, retention_days, owner_team, created_at, updated_at
) VALUES
    ('users', 'email', 'AUTH', TRUE, 'EMAIL', 'HASH_OR_MASK', 3650, 'backend', NOW(), NOW()),
    ('users', 'phone_number', 'AUTH', TRUE, 'PHONE', 'MASK_LAST4', 3650, 'backend', NOW(), NOW()),
    ('users', 'address', 'ORDER', TRUE, 'ADDRESS', 'MASK_PARTIAL', 3650, 'backend', NOW(), NOW()),
    ('payment_events', 'payload', 'PAYMENT', FALSE, NULL, NULL, 365, 'backend', NOW(), NOW()),
    ('request_logs', 'client_ip', 'OBSERVABILITY', TRUE, 'IP', 'TRUNCATE', 180, 'backend', NOW(), NOW());

INSERT IGNORE INTO batch_job_meta (
    job_name, schedule_desc, last_status, fail_count, created_at, updated_at
) VALUES
    ('metrics_hourly_aggregation', '매시 05분', 'INIT', 0, NOW(), NOW()),
    ('metrics_daily_aggregation', '매일 00:10', 'INIT', 0, NOW(), NOW());
