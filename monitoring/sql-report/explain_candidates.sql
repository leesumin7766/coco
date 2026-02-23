SELECT
    CONCAT('EXPLAIN ', REPLACE(SUBSTRING(sql_text, 1, 500), '\n', ' '), ';') AS explain_sql,
    COUNT(*) AS slow_count,
    ROUND(AVG(TIME_TO_SEC(query_time)), 4) AS avg_query_sec,
    ROUND(MAX(TIME_TO_SEC(query_time)), 4) AS max_query_sec
FROM mysql.slow_log
WHERE start_time >= NOW() - INTERVAL 1 DAY
GROUP BY sql_text
ORDER BY avg_query_sec DESC
LIMIT 20;
