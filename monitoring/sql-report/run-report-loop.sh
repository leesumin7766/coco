#!/usr/bin/env bash
set -euo pipefail

interval="${SQL_REPORT_INTERVAL_SECONDS:-86400}"
echo "[sql-report-runner] interval=${interval}s"

while true; do
  /bin/bash /work/monitoring/sql-report/run-report.sh || true
  sleep "${interval}"
done
