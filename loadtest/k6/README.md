# k6 Load Test (Docker)

## What this is
- Docker-based k6 runner so you do not need local installation
- Targets unauthenticated endpoints that exist in this project
- A/B runs can push k6 metrics to Prometheus and visualize in Grafana

## Assumptions
- `docker compose up -d` is running
- k6 runs inside the compose network (targets `backend:8080`)

## Scripts
- `smoke.js`: short, low-volume connectivity check
- `load.js`: sustained load to exercise DB/Hikari/threads metrics
- `peak.js`: peak load (ramp up, hold, ramp down)
- `product-detail-ab.js`: Redis 캐시 효과 측정을 위한 상품 상세 A/B 시나리오(hot/mixed/cold)
- `run-ab-storyline.sh`: A/B 실행 + Prometheus 스냅샷 + KPI 표 + 15분 스토리라인 자동 생성

## Run
```bash
docker compose run --rm k6 run /scripts/smoke.js
```

```bash
docker compose run --rm k6 run /scripts/load.js
```

```bash
docker compose run --rm k6 run /scripts/peak.js
```

### Redis 캐시 A/B 실행(스토리라인용)
사전 조건:
- `docker compose up -d` 로 스택이 실행 중이어야 함
- `docker-compose.override.yml`의 k6 서비스가 `./loadtest/results:/results` 마운트를 사용해야 함

실행:
```bash
cd /Users/isumin/coco
bash loadtest/k6/run-ab-storyline.sh
```

자동으로 backend 캐시 모드를 재시작까지 하려면:
```bash
AUTO_RESTART_BACKEND=1 bash loadtest/k6/run-ab-storyline.sh
```

## Customize
- Base URL (default: `http://backend:8080`)

```bash
docker compose run --rm -e BASE_URL=http://backend:8080 k6 run /scripts/load.js
```

- A/B 기본 파라미터
  - `HOT_ID` (default `1`)
  - `PRODUCT_IDS` (default `1,2,3,4,5,6,7,8,9,10`)
  - `REPEAT` (default `3`)
  - `VUS` (default `30`)
  - `DURATION` (default `60s`)
  - `SCENARIO_FOR_TABLE` (default `mixed`)

- k6 Prometheus remote-write 파라미터
  - `K6_RW_ENABLED` (default `1`)
  - `K6_RW_URL` (default `http://prometheus:9090/api/v1/write`)
  - `K6_RW_TREND_STATS` (default `p(50),p(95),p(99),avg,min,max`)
  - `K6_RW_PUSH_INTERVAL` (default `3s`)

## Grafana panels to watch
- `Threads Running`
- `Connection Usage`
- `Hikari Active/Pending`
- `DB CPU Usage`
- `Slow Query Rate` (if latency increases)
- `Alert List`

## k6 Grafana dashboard
- URL: `http://localhost:3000`
- Dashboard: `Coco / Coco k6 Load Test`
- 주요 패널:
  - `Total RPS`
  - `HTTP p95`
  - `HTTP Duration p95/p99`
  - `HTTP Error Rate by Scenario`
  - `VUs by Scenario`

## 어디서 성능 수치 확인하나?
아래 4곳을 보면 전/후 수치가 모두 맞춰집니다.

1. **k6 원본 응답성능**
- 위치: `loadtest/results/<timestamp>/before|after/<scenario>/run-*-summary.json`
- 확인값: `http_req_duration p(95), p(99)`, `http_reqs.rate`, `http_req_failed.rate`

2. **자동 집계 KPI 표**
- 위치: `loadtest/results/<timestamp>/kpi-table.md` / `kpi-table.csv`
- 항목: `API p95`, `API p99`, `RPS`, `SlowQuery/s`, `Hikari pending max`, `DB CPU%`, `Redis hit ratio%`
- 델타 계산:
  - 낮을수록 좋은 지표: `(Before - After) / Before * 100`
  - 높을수록 좋은 지표(RPS, hit ratio): `(After - Before) / Before * 100`

3. **Prometheus 즉시 조회**
- UI: `http://localhost:9090/graph`
- 스냅샷 파일: `loadtest/results/<timestamp>/before|after/prometheus-snapshot.json`
- 주요 PromQL:
  - `rate(mysql_global_status_slow_queries[5m])`
  - `max_over_time(hikaricp_connections_pending[5m])`
  - `increase(hikaricp_connections_timeout_total[5m])`
  - `sum(rate(container_cpu_usage_seconds_total{name=~".*coco-db.*"}[5m])) * 100`
  - `(rate(redis_keyspace_hits_total[5m]) / clamp_min(rate(redis_keyspace_hits_total[5m]) + rate(redis_keyspace_misses_total[5m]), 1)) * 100`

4. **Grafana 시각화**
- URL: `http://localhost:3000`
- Dashboard:
  - `Coco / Coco App + MariaDB`
  - `Coco / Coco k6 Load Test`

## 생성 산출물
- `loadtest/results/<timestamp>/kpi-table.md` : 발표용 KPI 표
- `loadtest/results/<timestamp>/storyline-script.md` : 15분 스토리라인 스크립트
