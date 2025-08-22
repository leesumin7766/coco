/* =========================================================
   안전 유틸리티: FK/컬럼이 존재할 때만 드랍
========================================================= */
DELIMITER //

CREATE PROCEDURE drop_fk_if_exists(IN tbl VARCHAR(64), IN fk VARCHAR(64))
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND TABLE_NAME = tbl
       AND CONSTRAINT_NAME = fk
       AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  ) THEN
    SET @s := CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk, '`');
    PREPARE x FROM @s; EXECUTE x; DEALLOCATE PREPARE x;
  END IF;
END//

CREATE PROCEDURE drop_col_if_exists(IN tbl VARCHAR(64), IN col VARCHAR(64))
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = tbl
       AND COLUMN_NAME = col
  ) THEN
    SET @s := CONCAT('ALTER TABLE `', tbl, '` DROP COLUMN `', col, '`');
    PREPARE x FROM @s; EXECUTE x; DEALLOCATE PREPARE x;
  END IF;
END//

/* =========================================================
   1) biddings: 오타 컬럼 → 정상 컬럼으로 데이터 이관 후 제거
========================================================= */
-- 정상 컬럼이 비어 있고 오타 컬럼에 값이 있으면 복사
UPDATE biddings
   SET bidding_position_id = COALESCE(bidding_position_id, bidding_posion_id)
 WHERE bidding_posion_id IS NOT NULL
   AND (bidding_position_id IS NULL OR bidding_position_id = 0);

-- 오타 컬럼에 걸린 자동 FK 제거
CALL drop_fk_if_exists('biddings','FK7sippo2aueqyelodtx5u5xbf8');  -- bidding_posion_id -> bidding_positions
-- 오타 컬럼 드랍
CALL drop_col_if_exists('biddings','bidding_posion_id');

/* =========================================================
   2) 중복 FK 정리: 명시적 fk_* 유지, 자동생성 FK* 제거
   (아래 목록은 실제 조회 결과 기반)
========================================================= */
-- biddings (정상 fk_biddings_* 유지)
CALL drop_fk_if_exists('biddings','FKomvddo1x8cbkuuklvp9tyvki1'); -- bidding_position_id dup
CALL drop_fk_if_exists('biddings','FKg861e8vxfr3w77j6xbehti4n0'); -- product_size_id dup
CALL drop_fk_if_exists('biddings','FK35bytyko04bcjp61mgbh8vm02'); -- status_id dup
CALL drop_fk_if_exists('biddings','FK7wlto2ej7jc1iltqnccn96miq'); -- user_id dup

-- orders (정상 fk_orders_* 유지)
CALL drop_fk_if_exists('orders','FK1gs72yw19wdxj4h8yt19mdqfs');  -- biddings_id dup
CALL drop_fk_if_exists('orders','FKhtx3insd5ge6w486omk4fnk54');  -- buyer_id dup
CALL drop_fk_if_exists('orders','FK2n7p8t83wo7x0lep1q06a6cvy');  -- order_status_id dup
CALL drop_fk_if_exists('orders','FKnpf21ips76010aqwc4t6yrcjp');  -- product_size_id dup
CALL drop_fk_if_exists('orders','FKsb9w6305d2be0rwbtifi7wymp');  -- seller_id dup

-- products (정상 fk_products_* 유지)
CALL drop_fk_if_exists('products','FKa3a4mpsfdf4d2y6r8ra3sc8mv'); -- brand_id dup
CALL drop_fk_if_exists('products','FKdb050tk37qryv15hd932626th'); -- user_id dup

-- product_images (정상 fk_product_images_product 유지)
CALL drop_fk_if_exists('product_images','FKqnq71xsohugpqwf3c9gxmsuy'); -- product_id dup

-- product_sizes (정상 fk_product_sizes_* 유지)
CALL drop_fk_if_exists('product_sizes','FK4isa0j51hpdn7cx04m831jic4'); -- product_id dup
CALL drop_fk_if_exists('product_sizes','FK3bqabm2nc8yyl9to7fo8trak4'); -- size_id dup

-- wishlists (정상 fk_wishlists_* 유지)
CALL drop_fk_if_exists('wishlists','FKl7ao98u2bm8nijc1rv4jobcrx');  -- product_id dup
CALL drop_fk_if_exists('wishlists','FK330pyw2el06fn5g28ypyljt16'); -- user_id dup

/* trades 테이블의 FK들은 로컬 기준 정의가 없고, 자동 FK만 존재하므로 유지합니다. */

/* 정리: 프로시저 제거(원하면 남겨둬도 무방) */
DROP PROCEDURE drop_fk_if_exists//
DROP PROCEDURE drop_col_if_exists//
DELIMITER ;
