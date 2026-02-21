-- 개발 환경 dev에서만 초기 데이터 세팅용

-- 1) 데이터베이스 생성 및 활성화
CREATE DATABASE IF NOT EXISTS yeezydb;
USE yeezydb;

-- 2) 상태 테이블 (status)
CREATE TABLE IF NOT EXISTS status (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NULL,
    PRIMARY KEY (id)
);

-- 3) 입찰 포지션 테이블 (bidding_positions)
CREATE TABLE IF NOT EXISTS bidding_positions (
    id INT NOT NULL AUTO_INCREMENT,
    position VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

-- 4) 브랜드 테이블 (brands)
CREATE TABLE IF NOT EXISTS brands (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- 5) 사이즈 테이블 (sizes)
CREATE TABLE IF NOT EXISTS sizes (
    id INT NOT NULL AUTO_INCREMENT,
    name INT NOT NULL,
    PRIMARY KEY (id)
);

-- 6) 사용자 테이블 (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    social_login_id VARCHAR(100) NULL,
    email VARCHAR(100) NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(50) NULL,
    phone_number VARCHAR(20) NULL,
    point INT NULL,
    address VARCHAR(200) NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    role VARCHAR(20) DEFAULT 'USER',
    PRIMARY KEY (id)
);

-- 7) 상품 테이블 (products)
CREATE TABLE IF NOT EXISTS products (
    id INT NOT NULL AUTO_INCREMENT,
    brand_id INT NOT NULL,
    name VARCHAR(100) NULL,
    name_kr VARCHAR(255) NULL,
    model_number VARCHAR(100) NOT NULL,
    release_price INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id)
);

-- 8) 상품 사이즈 매핑 테이블 (product_sizes)
CREATE TABLE IF NOT EXISTS product_sizes (
    id INT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    size_id INT NOT NULL,
    PRIMARY KEY (id)
);

-- 9) 입찰 테이블 (biddings)
CREATE TABLE IF NOT EXISTS biddings (
    id INT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status_id INT NOT NULL,
    bidding_position_id INT NOT NULL,
    product_size_id INT NOT NULL,
    price INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id)
);

-- 10) 주문 상태 테이블 (order_status)
CREATE TABLE IF NOT EXISTS order_status (
    id INT NOT NULL AUTO_INCREMENT,
    order_status VARCHAR(50) NULL,
    PRIMARY KEY (id)
);

-- 11) 주문 테이블 (orders)
CREATE TABLE IF NOT EXISTS orders (
    id INT NOT NULL AUTO_INCREMENT,
    order_status_id INT NOT NULL,
    biddings_id INT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    price INT NULL,
    product_size_id INT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    order_date DATETIME NULL,
    PRIMARY KEY (id)
);

-- 12) 찜 목록 테이블 (wishlists)
CREATE TABLE IF NOT EXISTS wishlists (
    id INT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    user_id BIGINT NOT NULL,
    size VARCHAR(10) NOT NULL,
    PRIMARY KEY (id)
);

-- 13) 상품 이미지 테이블 (product_images)
CREATE TABLE IF NOT EXISTS product_images (
    id INT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    image_url TEXT NOT NULL,
    PRIMARY KEY (id)
);

-- 14) 초기 데이터 삽입
INSERT INTO order_status (id, order_status) VALUES
    (1, 'PAYMENT_PENDING'),
    (2, 'PAYMENT_SUCCESS'),
    (3, 'DELIVERED'),
    (4, 'ORDER_CANCELLED');

INSERT INTO bidding_positions (position) VALUES
    ('SELL'),
    ('BUY');

INSERT INTO brands (id, name) VALUES
    (1, 'Nike'),
    (2, 'Adidas'),
    (3, 'New Balance'),
    (4, 'Puma'),
    (5, 'Reebok'),
    (10, 'others');

INSERT INTO sizes (id, name) VALUES
    (1, 210),
    (2, 220),
    (3, 230),
    (4, 240),
    (5, 250),
    (6, 260),
    (7, 270),
    (8, 280),
    (9, 290);

INSERT INTO status (name) VALUES
    ('PENDING'),
    ('MATCHED'),
    ('CANCELLED'),
    ('COMPLETED');

-- 15) 외래키 설정
ALTER TABLE products
    ADD CONSTRAINT fk_products_brand
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    ADD CONSTRAINT fk_products_user
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE product_sizes
    ADD CONSTRAINT fk_product_sizes_product
    FOREIGN KEY (product_id) REFERENCES products(id),
    ADD CONSTRAINT fk_product_sizes_size
    FOREIGN KEY (size_id) REFERENCES sizes(id);

ALTER TABLE biddings
    ADD CONSTRAINT fk_biddings_user
    FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_biddings_status
    FOREIGN KEY (status_id) REFERENCES status(id),
    ADD CONSTRAINT fk_biddings_position
    FOREIGN KEY (bidding_position_id) REFERENCES bidding_positions(id),
    ADD CONSTRAINT fk_biddings_product_size
    FOREIGN KEY (product_size_id) REFERENCES product_sizes(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_order_status
    FOREIGN KEY (order_status_id) REFERENCES order_status(id),
    ADD CONSTRAINT fk_orders_bidding
    FOREIGN KEY (biddings_id) REFERENCES biddings(id),
    ADD CONSTRAINT fk_orders_buyer
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    ADD CONSTRAINT fk_orders_seller
    FOREIGN KEY (seller_id) REFERENCES users(id),
    ADD CONSTRAINT fk_orders_product_size
    FOREIGN KEY (product_size_id) REFERENCES product_sizes(id);

ALTER TABLE wishlists
    ADD CONSTRAINT fk_wishlists_product
    FOREIGN KEY (product_id) REFERENCES products(id),
    ADD CONSTRAINT fk_wishlists_user
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE product_images
    ADD CONSTRAINT fk_product_images_product
    FOREIGN KEY (product_id) REFERENCES products(id);

-- 16) 관측/감사/이벤트/집계/메타데이터 테이블
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

-- 17) 최소 메타데이터 시드
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

-- 18) 스키마 강화 (감사/이벤트 신뢰성 + RDBMS 튜닝 인덱스)
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
