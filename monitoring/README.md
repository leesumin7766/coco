# DB Monitoring Setup (Compose)

## 1) Start stack
```bash
docker compose up -d
```

## 2) If DB volume already existed before this change
`/docker-entrypoint-initdb.d/*.sql` is only applied on first DB initialization.  
If your `db_data` already existed, apply these manually:

```bash
docker compose exec -T db mariadb -uroot -proot < sql/monitoring-grants.sql
docker compose exec -T db mariadb -uroot -proot < sql/monitoring-events.sql
```

## 3) Verify Prometheus targets
- `http://localhost:9090/targets`
- Expected UP jobs: `coco-backend`, `redis`, `mariadb`, `node`, `cadvisor`, `promtail`

## 4) Verify Grafana
- URL: `http://localhost:3000`
- Default account: `admin` / `admin`
- Provisioned dashboard: `Coco / Coco App + MariaDB`

## 5) Validate slow query pipeline
1. Run intentionally slow SQL (for example: `SELECT SLEEP(1)`).
2. Verify table source:
   ```sql
   SELECT start_time, query_time, sql_text
   FROM mysql.slow_log
   ORDER BY start_time DESC
   LIMIT 5;
   ```
3. Verify file source in Grafana logs panel (`job="mariadb-slowlog"`).

## 6) SQL periodic report
```bash
./monitoring/sql-report/run-report.sh
```

Output files are generated under:
`monitoring/sql-report/output/<timestamp>/`

## 7) Note for p95 query alert
`MariaDBQueryP95High` alert uses exporter metric
`mysql_perf_schema_eventsstatements_seconds_bucket`.
If this metric is unavailable in your MariaDB/exporter setup, the alert stays `NoData`.

## 8) Performance quantification flow (Redis cache A/B)
- k6 실행/집계: `/Users/isumin/coco/loadtest/k6/README.md`
- 결과 산출물:
  - `loadtest/results/<timestamp>/kpi-table.md`
  - `loadtest/results/<timestamp>/storyline-script.md`
- 지표 원본 확인:
  - Prometheus: `http://localhost:9090/graph`
  - Grafana: `http://localhost:3000` (`Coco / Coco App + MariaDB`)
