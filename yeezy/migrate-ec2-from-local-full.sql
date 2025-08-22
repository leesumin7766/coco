/* =========================================================
   0) 기본 설정 (현재 스키마 기준)
========================================================= */
SET @DB := DATABASE();

/* =========================================================
   1) 문자셋/콜레이션 통일 (DB + 각 테이블)
   - 테이블 리빌드가 발생할 수 있으니 트래픽 낮을 때 실행 권장
========================================================= */
SET @sql := CONCAT('ALTER DATABASE ', @DB, ' CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 필요한 테이블 목록: 존재할 때만 변환
SET @tables := (
  SELECT GROUP_CONCAT(TABLE_NAME)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @DB
    AND TABLE_NAME IN ('status','bidding_positions','brands','sizes',
                       'users','products','product_sizes','biddings',
                       'order_status','orders','wishlists','product_images')
);
-- 개별 테이블 변환
-- (동적 루프 대신 각 테이블 개별 변환문 생성)
ALTER TABLE status            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bidding_positions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE brands            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE sizes             CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE users             CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE products          CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE product_sizes     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE biddings          CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE order_status      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE orders            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE wishlists         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE product_images    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

/* =========================================================
   2) 누락 컬럼 보강 (IF NOT EXISTS) - 데이터 보존
========================================================= */
-- users.role
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS role VARCHAR(20) NULL DEFAULT 'USER';

-- products.user_id (있을 가능성이 높지만 안전하게)
ALTER TABLE products
  ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;

-- product_sizes: 필수 컬럼 존재 보장
ALTER TABLE product_sizes
  ADD COLUMN IF NOT EXISTS product_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS size_id INT NOT NULL;

-- biddings: 로컬 기준 컬럼 보강
ALTER TABLE biddings
  ADD COLUMN IF NOT EXISTS user_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS status_id INT NULL,
  ADD COLUMN IF NOT EXISTS bidding_position_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS product_size_id INT NULL,
  ADD COLUMN IF NOT EXISTS price INT NOT NULL;

-- orders: 로컬 기준 컬럼 보강
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS order_status_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS biddings_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS buyer_id BIGINT NOT NULL,
  ADD COLUMN IF NOT EXISTS seller_id BIGINT NOT NULL,
  ADD COLUMN IF NOT EXISTS price INT NULL,
  ADD COLUMN IF NOT EXISTS product_size_id INT NULL,
  ADD COLUMN IF NOT EXISTS order_date DATETIME NULL;

-- wishlists: 필수 컬럼 보강
ALTER TABLE wishlists
  ADD COLUMN IF NOT EXISTS product_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL,
  ADD COLUMN IF NOT EXISTS size VARCHAR(10) NOT NULL;

-- product_images
ALTER TABLE product_images
  ADD COLUMN IF NOT EXISTS product_id INT NOT NULL,
  ADD COLUMN IF NOT EXISTS image_url TEXT NOT NULL;

/* =========================================================
   3) 인덱스 보강 (외래키 컬럼용) - 중복 방지
   MariaDB 10.6: CREATE INDEX IF NOT EXISTS 지원
========================================================= */
CREATE INDEX IF NOT EXISTS idx_products_brand_id       ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_user_id        ON products(user_id);

CREATE INDEX IF NOT EXISTS idx_product_sizes_product   ON product_sizes(product_id);
CREATE INDEX IF NOT EXISTS idx_product_sizes_size      ON product_sizes(size_id);

CREATE INDEX IF NOT EXISTS idx_biddings_user_id        ON biddings(user_id);
CREATE INDEX IF NOT EXISTS idx_biddings_status_id      ON biddings(status_id);
CREATE INDEX IF NOT EXISTS idx_biddings_position_id    ON biddings(bidding_position_id);
CREATE INDEX IF NOT EXISTS idx_biddings_product_size   ON biddings(product_size_id);

CREATE INDEX IF NOT EXISTS idx_orders_status           ON orders(order_status_id);
CREATE INDEX IF NOT EXISTS idx_orders_biddings         ON orders(biddings_id);
CREATE INDEX IF NOT EXISTS idx_orders_buyer            ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller           ON orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_product_size     ON orders(product_size_id);

CREATE INDEX IF NOT EXISTS idx_wishlists_product       ON wishlists(product_id);
CREATE INDEX IF NOT EXISTS idx_wishlists_user          ON wishlists(user_id);

CREATE INDEX IF NOT EXISTS idx_product_images_product  ON product_images(product_id);

/* =========================================================
   4) 외래키(FK) 보강 (존재 시 스킵)
   - FK는 IF NOT EXISTS 직접 지원이 약해 정보스키마 체크 후 동적 실행
========================================================= */
DELIMITER //

-- 유틸: FK 없으면 실행
CREATE PROCEDURE add_fk_if_missing(IN tbl VARCHAR(64), IN fk_name VARCHAR(64), IN fk_sql TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = @DB
      AND TABLE_NAME = tbl
      AND CONSTRAINT_NAME = fk_name
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  ) THEN
    SET @s := fk_sql;
    PREPARE x FROM @s; EXECUTE x; DEALLOCATE PREPARE x;
  END IF;
END//

/* products */
CALL add_fk_if_missing('products','fk_products_brand',
  'ALTER TABLE products ADD CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id)');
CALL add_fk_if_missing('products','fk_products_user',
  'ALTER TABLE products ADD CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(id)');

/* product_sizes */
CALL add_fk_if_missing('product_sizes','fk_product_sizes_product',
  'ALTER TABLE product_sizes ADD CONSTRAINT fk_product_sizes_product FOREIGN KEY (product_id) REFERENCES products(id)');
CALL add_fk_if_missing('product_sizes','fk_product_sizes_size',
  'ALTER TABLE product_sizes ADD CONSTRAINT fk_product_sizes_size FOREIGN KEY (size_id) REFERENCES sizes(id)');

/* biddings */
CALL add_fk_if_missing('biddings','fk_biddings_user',
  'ALTER TABLE biddings ADD CONSTRAINT fk_biddings_user FOREIGN KEY (user_id) REFERENCES users(id)');
CALL add_fk_if_missing('biddings','fk_biddings_status',
  'ALTER TABLE biddings ADD CONSTRAINT fk_biddings_status FOREIGN KEY (status_id) REFERENCES status(id)');
CALL add_fk_if_missing('biddings','fk_biddings_position',
  'ALTER TABLE biddings ADD CONSTRAINT fk_biddings_position FOREIGN KEY (bidding_position_id) REFERENCES bidding_positions(id)');
CALL add_fk_if_missing('biddings','fk_biddings_product_size',
  'ALTER TABLE biddings ADD CONSTRAINT fk_biddings_product_size FOREIGN KEY (product_size_id) REFERENCES product_sizes(id)');

/* orders */
CALL add_fk_if_missing('orders','fk_orders_order_status',
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_order_status FOREIGN KEY (order_status_id) REFERENCES order_status(id)');
CALL add_fk_if_missing('orders','fk_orders_bidding',
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_bidding FOREIGN KEY (biddings_id) REFERENCES biddings(id)');
CALL add_fk_if_missing('orders','fk_orders_buyer',
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id)');
CALL add_fk_if_missing('orders','fk_orders_seller',
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id)');
CALL add_fk_if_missing('orders','fk_orders_product_size',
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_product_size FOREIGN KEY (product_size_id) REFERENCES product_sizes(id)');

/* wishlists */
CALL add_fk_if_missing('wishlists','fk_wishlists_product',
  'ALTER TABLE wishlists ADD CONSTRAINT fk_wishlists_product FOREIGN KEY (product_id) REFERENCES products(id)');
CALL add_fk_if_missing('wishlists','fk_wishlists_user',
  'ALTER TABLE wishlists ADD CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id)');

/* product_images */
CALL add_fk_if_missing('product_images','fk_product_images_product',
  'ALTER TABLE product_images ADD CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)');

DROP PROCEDURE add_fk_if_missing//
DELIMITER ;

/* =========================================================
   5) 기준(레퍼런스) 데이터 보강 (중복 무시)
========================================================= */
INSERT IGNORE INTO bidding_positions (id, position) VALUES (1,'SELL'),(2,'BUY');
INSERT IGNORE INTO order_status (id, order_status) VALUES
  (1,'PAYMENT_PENDING'),(2,'PAYMENT_SUCCESS'),(3,'DELIVERED'),(4,'ORDER_CANCELLED');
INSERT IGNORE INTO brands (id, name) VALUES
  (1,'Nike'),(2,'Adidas'),(3,'New Balance'),(4,'Puma'),(5,'Reebok'),(10,'others');

/* =========================================================
   끝
========================================================= */
