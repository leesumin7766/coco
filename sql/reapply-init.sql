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

-- 스키마 강화 (재기동 시에도 안전하게 재실행)
ALTER TABLE payment_events
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS prev_status VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS new_status VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS event_version INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS source_service VARCHAR(60) NOT NULL DEFAULT 'backend',
    ADD COLUMN IF NOT EXISTS actor_user_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE trade_events
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS prev_status VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS new_status VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS event_version INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS source_service VARCHAR(60) NOT NULL DEFAULT 'backend',
    ADD COLUMN IF NOT EXISTS actor_user_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS source_service VARCHAR(60) NOT NULL DEFAULT 'backend',
    ADD COLUMN IF NOT EXISTS event_version INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_request_logs_trace_created_at ON request_logs (trace_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target_created_at ON audit_logs (target_type, target_id, created_at);
CREATE INDEX IF NOT EXISTS idx_payment_events_trace_created_at ON payment_events (trace_id, created_at);
CREATE INDEX IF NOT EXISTS idx_trade_events_trace_created_at ON trade_events (trace_id, created_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_events_idempotency ON payment_events (order_id, event_type, idempotency_key);
CREATE UNIQUE INDEX IF NOT EXISTS uk_trade_events_idempotency ON trade_events (event_type, idempotency_key);

CREATE INDEX IF NOT EXISTS idx_biddings_match ON biddings (product_size_id, bidding_position_id, status_id, price, id);
CREATE INDEX IF NOT EXISTS idx_biddings_user_status_created_at ON biddings (user_id, status_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_created_at ON orders (buyer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_seller_created_at ON orders (seller_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status_created_at ON orders (order_status_id, created_at);
CREATE INDEX IF NOT EXISTS idx_product_sizes_product_size ON product_sizes (product_id, size_id);
-- 모니터링 전용 계정 (idempotent)
CREATE USER IF NOT EXISTS 'mysql_exporter'@'%' IDENTIFIED BY 'mysql_exporter_password';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'mysql_exporter'@'%';

CREATE USER IF NOT EXISTS 'grafana_reader'@'%' IDENTIFIED BY 'grafana_reader_password';
GRANT SELECT ON mysql.* TO 'grafana_reader'@'%';
GRANT SELECT ON yeezydb.* TO 'grafana_reader'@'%';
FLUSH PRIVILEGES;

-- metrics_hourly 스냅샷 이벤트
SET GLOBAL event_scheduler = ON;

CREATE DEFINER='root'@'localhost' EVENT IF NOT EXISTS ev_metrics_hourly_table_size
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP + INTERVAL 5 MINUTE
DO
INSERT INTO metrics_hourly (
    bucket_start, metric_name, dimension_key, metric_value, created_at, updated_at
)
SELECT
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'),
    'table_total_bytes',
    table_name,
    IFNULL(data_length, 0) + IFNULL(index_length, 0),
    NOW(),
    NOW()
FROM information_schema.tables
WHERE table_schema = 'yeezydb'
ON DUPLICATE KEY UPDATE
    metric_value = VALUES(metric_value),
    updated_at = VALUES(updated_at);

CREATE DEFINER='root'@'localhost' EVENT IF NOT EXISTS ev_metrics_hourly_index_size
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP + INTERVAL 6 MINUTE
DO
INSERT INTO metrics_hourly (
    bucket_start, metric_name, dimension_key, metric_value, created_at, updated_at
)
SELECT
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'),
    'index_total_bytes',
    table_name,
    IFNULL(index_length, 0),
    NOW(),
    NOW()
FROM information_schema.tables
WHERE table_schema = 'yeezydb'
ON DUPLICATE KEY UPDATE
    metric_value = VALUES(metric_value),
    updated_at = VALUES(updated_at);

CREATE DEFINER='root'@'localhost' EVENT IF NOT EXISTS ev_metrics_hourly_binlog
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP + INTERVAL 7 MINUTE
DO
INSERT INTO metrics_hourly (
    bucket_start, metric_name, dimension_key, metric_value, created_at, updated_at
)
SELECT
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'),
    'binlog_bytes_written',
    'global',
    CAST(variable_value AS UNSIGNED),
    NOW(),
    NOW()
FROM information_schema.global_status
WHERE variable_name = 'BINLOG_BYTES_WRITTEN'
ON DUPLICATE KEY UPDATE
    metric_value = VALUES(metric_value),
    updated_at = VALUES(updated_at);

CREATE DEFINER='root'@'localhost' EVENT IF NOT EXISTS ev_metrics_daily_rollup
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP + INTERVAL 10 MINUTE
DO
INSERT INTO metrics_daily (
    metric_date, metric_name, dimension_key, metric_value, created_at, updated_at
)
SELECT
    DATE(bucket_start),
    metric_name,
    dimension_key,
    MAX(metric_value),
    NOW(),
    NOW()
FROM metrics_hourly
WHERE bucket_start >= CURRENT_DATE - INTERVAL 1 DAY
GROUP BY DATE(bucket_start), metric_name, dimension_key
ON DUPLICATE KEY UPDATE
    metric_value = VALUES(metric_value),
    updated_at = VALUES(updated_at);
