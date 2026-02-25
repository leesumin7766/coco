#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILES=(-f "${REPO_ROOT}/docker-compose.yml" -f "${REPO_ROOT}/docker-compose.override.yml")

TIMESTAMP="${TIMESTAMP:-$(date +%Y%m%d-%H%M%S)}"
RESULT_ROOT="${RESULT_ROOT:-${REPO_ROOT}/loadtest/results}"
RESULT_DIR="${RESULT_ROOT}/${TIMESTAMP}"

MODE="${MODE:-before}" # before|after
BASE_URL="${BASE_URL:-http://backend:8080}"
K6_RW_ENABLED="${K6_RW_ENABLED:-1}"
K6_RW_URL="${K6_RW_URL:-http://prometheus:9090/api/v1/write}"
K6_RW_TREND_STATS="${K6_RW_TREND_STATS:-p(50),p(95),p(99),avg,min,max}"
K6_RW_PUSH_INTERVAL="${K6_RW_PUSH_INTERVAL:-3s}"

HOT_ID="${HOT_ID:-1}"
PRODUCT_IDS="${PRODUCT_IDS:-1,2,3,4,5,6,7,8,9,10}"
SCENARIOS="${SCENARIOS:-hot,mixed}" # cold 제외 기본

# RPS plan
WARMUP_ENABLED="${WARMUP_ENABLED:-1}"
WARMUP_STAGE="${WARMUP_STAGE:-5m:500}"
MAIN_STAGES="${MAIN_STAGES:-3m:1000,3m:2000,3m:5000}"
START_RPS="${START_RPS:-1}"
PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS:-300}"
MAX_VUS="${MAX_VUS:-3000}"

SLEEP_MS="${SLEEP_MS:-0}"

require_bin() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

run_one() {
  local scenario="$1"
  local phase="$2"
  local stages="$3"
  local summary="/results/${TIMESTAMP}/${MODE}/${scenario}/${phase}-summary.json"
  local logfile="${RESULT_DIR}/${MODE}/${scenario}/${phase}.log"
  local k6_output_args=()

  if [[ "${K6_RW_ENABLED}" == "1" ]]; then
    k6_output_args=(-o "experimental-prometheus-rw")
  fi

  echo "[${MODE}] scenario=${scenario} phase=${phase} stages=${stages}"
  docker compose "${COMPOSE_FILES[@]}" run --rm \
    -e BASE_URL="${BASE_URL}" \
    -e SCENARIO="${scenario}" \
    -e HOT_ID="${HOT_ID}" \
    -e PRODUCT_IDS="${PRODUCT_IDS}" \
    -e START_RPS="${START_RPS}" \
    -e RPS_STAGES="${stages}" \
    -e PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS}" \
    -e MAX_VUS="${MAX_VUS}" \
    -e SLEEP_MS="${SLEEP_MS}" \
    -e PHASE="${phase}" \
    -e K6_PROMETHEUS_RW_SERVER_URL="${K6_RW_URL}" \
    -e K6_PROMETHEUS_RW_TREND_STATS="${K6_RW_TREND_STATS}" \
    -e K6_PROMETHEUS_RW_PUSH_INTERVAL="${K6_RW_PUSH_INTERVAL}" \
    k6 run --quiet \
      --summary-export "${summary}" \
      --tag "mode=${MODE}" \
      --tag "scenario=${scenario}" \
      --tag "phase=${phase}" \
      --tag "plan=rps" \
      "${k6_output_args[@]}" \
      /scripts/product-detail-rps.js | tee "${logfile}"
}

require_bin docker

mkdir -p "${RESULT_DIR}/${MODE}"
chmod -R 777 "${RESULT_DIR}" || true

IFS=',' read -r -a scenario_arr <<< "${SCENARIOS}"
for sc in "${scenario_arr[@]}"; do
  sc="$(echo "$sc" | xargs)"
  [[ -z "$sc" ]] && continue
  mkdir -p "${RESULT_DIR}/${MODE}/${sc}"
  chmod -R 777 "${RESULT_DIR}/${MODE}/${sc}" || true

  if [[ "${WARMUP_ENABLED}" == "1" ]]; then
    run_one "${sc}" "warmup" "${WARMUP_STAGE}"
  fi
  run_one "${sc}" "main" "${MAIN_STAGES}"
done

cat <<MSG

Done.
- mode: ${MODE}
- scenarios: ${SCENARIOS}
- results: loadtest/results/${TIMESTAMP}/${MODE}

MSG
