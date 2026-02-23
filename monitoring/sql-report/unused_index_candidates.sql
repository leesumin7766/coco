SELECT
    OBJECT_SCHEMA AS table_schema,
    OBJECT_NAME AS table_name,
    INDEX_NAME AS index_name,
    COUNT_STAR AS index_access_count
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'yeezydb'
  AND INDEX_NAME IS NOT NULL
  AND INDEX_NAME <> 'PRIMARY'
  AND COUNT_STAR = 0
ORDER BY OBJECT_NAME, INDEX_NAME;
