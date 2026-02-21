package com.example.shop.observability.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetricAggregationService {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 5 * * * *")
    @Transactional
    public void aggregatePreviousHour() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime bucketStart = now.withMinute(0).withSecond(0).withNano(0).minusHours(1);
            LocalDateTime bucketEnd = bucketStart.plusHours(1);

            upsertHourlyMetric(bucketStart, "request.count", "all",
                    count("SELECT COUNT(*) FROM request_logs WHERE created_at >= ? AND created_at < ?", bucketStart, bucketEnd));

            upsertHourlyMetric(bucketStart, "request.error.count", "all",
                    count("SELECT COUNT(*) FROM request_logs WHERE status_code >= 500 AND created_at >= ? AND created_at < ?", bucketStart, bucketEnd));

            upsertHourlyMetric(bucketStart, "payment.confirmed.count", "all",
                    count("SELECT COUNT(*) FROM payment_events WHERE status = 'CONFIRMED' AND created_at >= ? AND created_at < ?", bucketStart, bucketEnd));

            upsertHourlyMetric(bucketStart, "trade.matched.count", "all",
                    count("SELECT COUNT(*) FROM trade_events WHERE status = 'MATCHED' AND created_at >= ? AND created_at < ?", bucketStart, bucketEnd));

            updateBatchMeta("metrics_hourly_aggregation", "매시 05분", "SUCCESS", 0);
        } catch (Exception e) {
            updateBatchMeta("metrics_hourly_aggregation", "매시 05분", "FAILED", 1);
            throw e;
        }
    }

    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void aggregatePreviousDay() {
        try {
            LocalDate day = LocalDate.now().minusDays(1);

            upsertDailyMetric(day, "request.count", "all",
                    sumHourly(day, "request.count", "all"));

            upsertDailyMetric(day, "request.error.count", "all",
                    sumHourly(day, "request.error.count", "all"));

            upsertDailyMetric(day, "payment.confirmed.count", "all",
                    sumHourly(day, "payment.confirmed.count", "all"));

            upsertDailyMetric(day, "trade.matched.count", "all",
                    sumHourly(day, "trade.matched.count", "all"));

            updateBatchMeta("metrics_daily_aggregation", "매일 00:10", "SUCCESS", 0);
        } catch (Exception e) {
            updateBatchMeta("metrics_daily_aggregation", "매일 00:10", "FAILED", 1);
            throw e;
        }
    }

    private long count(String sql, LocalDateTime start, LocalDateTime end) {
        Long result = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
        return result == null ? 0L : result;
    }

    private long sumHourly(LocalDate day, String metricName, String dimensionKey) {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(metric_value), 0) FROM metrics_hourly WHERE DATE(bucket_start) = ? AND metric_name = ? AND dimension_key = ?",
                Long.class,
                day,
                metricName,
                dimensionKey
        );
        return result == null ? 0L : result;
    }

    private void upsertHourlyMetric(LocalDateTime bucketStart, String metricName, String dimensionKey, long metricValue) {
        jdbcTemplate.update(
                """
                INSERT INTO metrics_hourly (bucket_start, metric_name, dimension_key, metric_value, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE metric_value = VALUES(metric_value), updated_at = NOW()
                """,
                Timestamp.valueOf(bucketStart),
                metricName,
                dimensionKey,
                metricValue
        );
    }

    private void upsertDailyMetric(LocalDate metricDate, String metricName, String dimensionKey, long metricValue) {
        jdbcTemplate.update(
                """
                INSERT INTO metrics_daily (metric_date, metric_name, dimension_key, metric_value, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE metric_value = VALUES(metric_value), updated_at = NOW()
                """,
                metricDate,
                metricName,
                dimensionKey,
                metricValue
        );
    }

    private void updateBatchMeta(String jobName, String scheduleDesc, String status, int failDelta) {
        jdbcTemplate.update(
                """
                INSERT INTO batch_job_meta (job_name, schedule_desc, last_run_at, last_success_at, last_status, fail_count, created_at, updated_at)
                VALUES (?, ?, NOW(), CASE WHEN ? = 'SUCCESS' THEN NOW() ELSE NULL END, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    schedule_desc = VALUES(schedule_desc),
                    last_run_at = NOW(),
                    last_success_at = CASE WHEN VALUES(last_status) = 'SUCCESS' THEN NOW() ELSE last_success_at END,
                    last_status = VALUES(last_status),
                    fail_count = CASE
                        WHEN VALUES(last_status) = 'FAILED' THEN fail_count + ?
                        WHEN VALUES(last_status) = 'SUCCESS' THEN 0
                        ELSE fail_count
                    END,
                    updated_at = NOW()
                """,
                jobName,
                scheduleDesc,
                status,
                status,
                failDelta,
                failDelta
        );
    }
}
