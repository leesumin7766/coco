USE yeezydb;

SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS ev_metrics_hourly_table_size
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

CREATE EVENT IF NOT EXISTS ev_metrics_hourly_index_size
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

CREATE EVENT IF NOT EXISTS ev_metrics_hourly_binlog
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

CREATE EVENT IF NOT EXISTS ev_metrics_daily_rollup
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
