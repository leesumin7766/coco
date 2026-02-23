WITH index_columns AS (
    SELECT
        TABLE_SCHEMA,
        TABLE_NAME,
        INDEX_NAME,
        GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns_joined
    FROM information_schema.statistics
    WHERE TABLE_SCHEMA = 'yeezydb'
    GROUP BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME
)
SELECT
    i1.TABLE_NAME,
    i1.INDEX_NAME AS candidate_redundant_index,
    i2.INDEX_NAME AS candidate_covering_index,
    i1.columns_joined AS redundant_columns,
    i2.columns_joined AS covering_columns
FROM index_columns i1
JOIN index_columns i2
  ON i1.TABLE_SCHEMA = i2.TABLE_SCHEMA
 AND i1.TABLE_NAME = i2.TABLE_NAME
 AND i1.INDEX_NAME <> i2.INDEX_NAME
 AND (
     i1.columns_joined = i2.columns_joined
     OR i2.columns_joined LIKE CONCAT(i1.columns_joined, ',%')
 )
ORDER BY i1.TABLE_NAME, i1.INDEX_NAME;
