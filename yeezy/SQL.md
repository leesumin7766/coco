create yeezydb;
use yeezydb;
show TABLES
DESCRIBE users;

ALTER TABLE users MODIFY id INT NOT NULL AUTO_INCREMENT;
ALTER TABLE users MODIFY COLUMN id NULL;
ALTER TABLE users MODIFY COLUMN created_at NULL;

# 브랜드 추가
INSERT INTO brands (id, name) VALUES
(1, 'Nike'),
(2, 'Adidas'),
(3, 'New Balance'),
(4, 'Puma'),
(5, 'Reebok'),
(10, 'others');
SELECT * from brands;
# 사이즈 추가
INSERT INTO sizes (id, name) VALUES
(1, '210'),
(2, '220'),
(3, '230'),
(4, '240'),
(5, '250'),
(6, '260'),
(7, '270'),
(8, '280'),
(9, '290');
SELECT * FROM sizes;

INSERT INTO order_status (id, order_status) VALUES
(1, 'PAYMENT_PENDING'),
(2, 'PAYMENT_SUCCESS'),
(3, 'DELIVERED'),
(4, 'ORDER_CANCELLED');

ALTER TABLE users MODIFY id INT NOT NULL AUTO_INCREMENT;
ALTER TABLE users MODIFY COLUMN id NULL;
ALTER TABLE users MODIFY COLUMN created_at NULL;
alter table orders add column order_date DATETIME;
ALTER TABLE product_images MODIFY image_url TEXT;
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER';
ALTER TABLE wishlists ADD COLUMN size VARCHAR(10) NOT NULL;

ALTER TABLE products ADD COLUMN name_kr VARCHAR(255);

ALTER TABLE orders
ADD COLUMN price INT,
ADD COLUMN product_size_id INT;

# 테이블 추가
CREATE TABLE `status` (
`id` INT NOT NULL AUTO_INCREMENT,
`name` VARCHAR(50) NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `bidding_positions` (
`id` INT NOT NULL AUTO_INCREMENT,
`position` VARCHAR(50) NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `brands` (
`id` INT NOT NULL AUTO_INCREMENT,
`name` VARCHAR(100) NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `sizes` (
`id` INT NOT NULL AUTO_INCREMENT,
`name` INT NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `users` (
`id` INT NOT NULL AUTO_INCREMENT,
`social_login_id` VARCHAR(100) NULL,
`email` VARCHAR(100) NULL,
`password` VARCHAR(255) NULL,
`name` VARCHAR(50) NULL,
`phone_number` VARCHAR(20) NULL,
`point` INT NULL,
`address` VARCHAR(200) NOT NULL,
`created_at` DATETIME NOT NULL,
`updated_at` DATETIME NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `products` (
`id` INT NOT NULL AUTO_INCREMENT,
`brand_id` INT NOT NULL,
`name` VARCHAR(100) NULL,
`model_number` VARCHAR(100) NOT NULL,
`release_price` INT NOT NULL,
`created_at` DATETIME NOT NULL,
`updated_at` DATETIME NULL,
`deleted_at` DATETIME NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `product_sizes` (
`id` INT NOT NULL AUTO_INCREMENT,
`product_id` INT NOT NULL,
`size_id` INT NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `biddings` (
`id` INT NOT NULL AUTO_INCREMENT,
`user_id` INT NOT NULL,
`status_id` INT NOT NULL,
`bidding_posion_id` INT NOT NULL,
`product_size_id` INT NOT NULL,
`price` INT NOT NULL,
`created_at` DATETIME NOT NULL,
`updated_at` DATETIME NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `orders` (
`id` INT NOT NULL AUTO_INCREMENT,
`order_status_id` INT NOT NULL,
`biddings_id` INT NOT NULL,
`buyer_id` INT NOT NULL,
`seller_id` INT NOT NULL,
`created_at` DATETIME NOT NULL,
`updated_at` DATETIME NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `order_status` (
`id` INT NOT NULL AUTO_INCREMENT,
`order_status` VARCHAR(50) NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `wishlists` (
`id` INT NOT NULL AUTO_INCREMENT,
`product_id` INT NOT NULL,
`user_id` INT NOT NULL,
PRIMARY KEY (`id`)
);

CREATE TABLE `product_images` (
`id` INT NOT NULL AUTO_INCREMENT,
`product_id` INT NOT NULL,
`image_url` VARCHAR(1000) NOT NULL,
PRIMARY KEY (`id`)
);


