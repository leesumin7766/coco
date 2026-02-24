#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILES=(-f "${REPO_ROOT}/docker-compose.yml" -f "${REPO_ROOT}/docker-compose.override.yml")
RESULT_ROOT="${RESULT_ROOT:-${REPO_ROOT}/loadtest/results}"
TIMESTAMP="${TIMESTAMP:-$(date +%Y%m%d-%H%M%S)}"
RESULT_DIR="${RESULT_ROOT}/${TIMESTAMP}"

BASE_URL="${BASE_URL:-http://backend:8080}"
PROM_URL="${PROM_URL:-http://localhost:9090}"
HOT_ID="${HOT_ID:-1}"
PRODUCT_IDS="${PRODUCT_IDS:-1,2,3,4,5,6,7,8,9,10}"
REPEAT="${REPEAT:-3}"
VUS="${VUS:-30}"
DURATION="${DURATION:-60s}"
SLEEP_MS="${SLEEP_MS:-0}"
SCENARIO_FOR_TABLE="${SCENARIO_FOR_TABLE:-mixed}"
AUTO_RESTART_BACKEND="${AUTO_RESTART_BACKEND:-0}"
BACKEND_READY_TIMEOUT_SEC="${BACKEND_READY_TIMEOUT_SEC:-180}"
POST_RESTART_SLEEP_SEC="${POST_RESTART_SLEEP_SEC:-20}"

# k6 -> Prometheus remote-write
K6_RW_ENABLED="${K6_RW_ENABLED:-1}"
K6_RW_URL="${K6_RW_URL:-http://prometheus:9090/api/v1/write}"
K6_RW_TREND_STATS="${K6_RW_TREND_STATS:-p(50),p(95),p(99),avg,min,max}"
K6_RW_PUSH_INTERVAL="${K6_RW_PUSH_INTERVAL:-3s}"

require_bin() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

wait_backend_ready() {
  local waited=0
  local query_url="${PROM_URL}/api/v1/query?query=up%7Bjob%3D%22coco-backend%22%7D"
  echo "Waiting for backend readiness via Prometheus up{job=\"coco-backend\"}..."
  while true; do
    if curl -fsS "${query_url}" | grep -q '"value":\[.*,"1"\]'; then
      echo "Backend is ready."
      break
    fi
    sleep 2
    waited=$((waited + 2))
    if [[ "${waited}" -ge "${BACKEND_READY_TIMEOUT_SEC}" ]]; then
      echo "Backend readiness wait timed out after ${BACKEND_READY_TIMEOUT_SEC}s" >&2
      exit 1
    fi
  done
}

run_mode() {
  local label="$1"
  local cache_type="$2"

  echo ""
  echo "========== ${label} (SPRING_CACHE_TYPE=${cache_type}) =========="

  if [[ "${AUTO_RESTART_BACKEND}" == "1" ]]; then
    SPRING_CACHE_TYPE="${cache_type}" docker compose "${COMPOSE_FILES[@]}" up -d --force-recreate backend
    wait_backend_ready
    sleep "${POST_RESTART_SLEEP_SEC}"
  else
    echo "AUTO_RESTART_BACKEND=0"
    echo "Please ensure backend is running with: SPRING_CACHE_TYPE=${cache_type}"
    read -r -p "Press Enter to continue..."
  fi

  mkdir -p "${RESULT_DIR}/${label}"

  for scenario in hot mixed cold; do
    mkdir -p "${RESULT_DIR}/${label}/${scenario}"
    for run in $(seq 1 "${REPEAT}"); do
      local summary="/results/${TIMESTAMP}/${label}/${scenario}/run-${run}-summary.json"
      local logfile="${RESULT_DIR}/${label}/${scenario}/run-${run}.log"
      local k6_output_args=()

      if [[ "${K6_RW_ENABLED}" == "1" ]]; then
        k6_output_args=(-o "experimental-prometheus-rw")
      fi

      echo "[${label}] scenario=${scenario} run=${run}/${REPEAT}"
      docker compose "${COMPOSE_FILES[@]}" run --rm \
        -e BASE_URL="${BASE_URL}" \
        -e SCENARIO="${scenario}" \
        -e HOT_ID="${HOT_ID}" \
        -e PRODUCT_IDS="${PRODUCT_IDS}" \
        -e VUS="${VUS}" \
        -e DURATION="${DURATION}" \
        -e SLEEP_MS="${SLEEP_MS}" \
        -e K6_PROMETHEUS_RW_SERVER_URL="${K6_RW_URL}" \
        -e K6_PROMETHEUS_RW_TREND_STATS="${K6_RW_TREND_STATS}" \
        -e K6_PROMETHEUS_RW_PUSH_INTERVAL="${K6_RW_PUSH_INTERVAL}" \
        k6 run --quiet \
          --summary-export "${summary}" \
          --tag "mode=${label}" \
          --tag "scenario=${scenario}" \
          --tag "run=${run}" \
          "${k6_output_args[@]}" \
          /scripts/product-detail-ab.js | tee "${logfile}"
    done
  done

  node "${SCRIPT_DIR}/aggregate-k6.mjs" \
    --input "${RESULT_DIR}/${label}" \
    --output "${RESULT_DIR}/${label}/k6-median.csv"

  node "${SCRIPT_DIR}/query-prometheus.mjs" \
    --base-url "${PROM_URL}" \
    --output "${RESULT_DIR}/${label}/prometheus-snapshot.json"
}

require_bin docker
require_bin node

mkdir -p "${RESULT_DIR}"

run_mode before none
run_mode after redis

node "${SCRIPT_DIR}/build-kpi-table.mjs" \
  --before-k6 "${RESULT_DIR}/before/k6-median.csv" \
  --after-k6 "${RESULT_DIR}/after/k6-median.csv" \
  --before-prom "${RESULT_DIR}/before/prometheus-snapshot.json" \
  --after-prom "${RESULT_DIR}/after/prometheus-snapshot.json" \
  --scenario "${SCENARIO_FOR_TABLE}" \
  --out-csv "${RESULT_DIR}/kpi-table.csv" \
  --out-md "${RESULT_DIR}/kpi-table.md"

node "${SCRIPT_DIR}/generate-storyline.mjs" \
  --kpi "${RESULT_DIR}/kpi-table.csv" \
  --baseline-date "2026-02-23" \
  --output "${RESULT_DIR}/storyline-script.md"

cat <<MSG

Done.
- k6 summaries/logs: loadtest/results/${TIMESTAMP}/before, loadtest/results/${TIMESTAMP}/after
- KPI table: loadtest/results/${TIMESTAMP}/kpi-table.md
- Storyline script: loadtest/results/${TIMESTAMP}/storyline-script.md

MSG
