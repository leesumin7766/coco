# k6 Load Test (Docker)

## What this is
- Docker-based k6 runner so you do not need local installation
- Targets unauthenticated endpoints that exist in this project

## Assumptions
- `docker compose up -d` is running
- k6 runs inside the compose network (targets `backend:8080`)

## Scripts
- `smoke.js`: short, low-volume connectivity check
- `load.js`: sustained load to exercise DB/Hikari/threads metrics
- `peak.js`: peak load (ramp up, hold, ramp down)

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

## Customize
- Base URL (default: `http://backend:8080`)

```bash
docker compose run --rm -e BASE_URL=http://backend:8080 k6 run /scripts/load.js
```

## Grafana panels to watch
- `Threads Running`
- `Connection Usage`
- `Hikari Active/Pending`
- `DB CPU Usage`
- `Slow Query Rate` (if latency increases)
- `Alert List`
