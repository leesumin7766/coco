#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="${SCRIPT_DIR}/output/$(date +%Y%m%d-%H%M%S)"
mkdir -p "${OUT_DIR}"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3307}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
MYSQL_DB="${MYSQL_DB:-yeezydb}"

run_report() {
  local sql_file="$1"
  local out_file="$2"

  mysql \
    --host="${MYSQL_HOST}" \
    --port="${MYSQL_PORT}" \
    --user="${MYSQL_USER}" \
    --password="${MYSQL_PASSWORD}" \
    --database="${MYSQL_DB}" \
    --table < "${SCRIPT_DIR}/${sql_file}" > "${OUT_DIR}/${out_file}"
}

run_report top_table_scan.sql top_table_scan.txt
run_report duplicate_index_candidates.sql duplicate_index_candidates.txt
run_report unused_index_candidates.sql unused_index_candidates.txt
run_report explain_candidates.sql explain_candidates.txt

cat <<MSG
SQL report created:
  ${OUT_DIR}/top_table_scan.txt
  ${OUT_DIR}/duplicate_index_candidates.txt
  ${OUT_DIR}/unused_index_candidates.txt
  ${OUT_DIR}/explain_candidates.txt
MSG
