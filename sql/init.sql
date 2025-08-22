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
