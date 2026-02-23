SELECT
    DIGEST_TEXT AS query_sample,
    COUNT_STAR AS exec_count,
    ROUND(SUM_ROWS_EXAMINED / NULLIF(COUNT_STAR, 0), 2) AS avg_rows_examined,
    ROUND(SUM_TIMER_WAIT / 1000000000000, 3) AS total_exec_sec
FROM performance_schema.events_statements_summary_by_digest
WHERE DIGEST_TEXT IS NOT NULL
ORDER BY SUM_ROWS_EXAMINED DESC
LIMIT 20;
