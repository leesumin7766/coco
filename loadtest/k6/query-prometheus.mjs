#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';

function parseArgs(argv) {
  const args = {};
  for (let i = 2; i < argv.length; i += 1) {
    const key = argv[i];
    const value = argv[i + 1];
    if (!key.startsWith('--') || value == null) continue;
    args[key.slice(2)] = value;
    i += 1;
  }
  return args;
}

const args = parseArgs(process.argv);
const baseUrl = (args['base-url'] || 'http://localhost:9090').replace(/\/+$/, '');
const output = args.output || './prometheus-snapshot.json';
const dbCpuPrimary = 'sum(rate(container_cpu_usage_seconds_total{name=~".*coco-db.*"}[5m])) * 100';
const dbCpuFallback = 'sum(rate(namedprocess_namegroup_cpu_seconds_total{groupname="mariadbd"}[5m])) * 100';

const queries = {
  slow_query_per_sec: 'rate(mysql_global_status_slow_queries[5m])',
  hikari_pending_max_5m: 'max_over_time(hikaricp_connections_pending[5m])',
  hikari_timeout_increase_5m: 'increase(hikaricp_connections_timeout_total[5m])',
  db_connection_usage_pct:
    '(mysql_global_status_threads_connected / mysql_global_variables_max_connections) * 100',
  row_lock_waits_per_sec: 'rate(mysql_global_status_innodb_row_lock_waits[5m])',
  threads_running: 'mysql_global_status_threads_running',
  redis_hit_ratio_pct:
    '(rate(redis_keyspace_hits_total[5m]) / clamp_min(rate(redis_keyspace_hits_total[5m]) + rate(redis_keyspace_misses_total[5m]), 1)) * 100',
  redis_used_memory_bytes: 'redis_memory_used_bytes',
  redis_connected_clients: 'redis_connected_clients',
};

async function queryOne(promql) {
  const url = `${baseUrl}/api/v1/query?query=${encodeURIComponent(promql)}`;
  try {
    const res = await fetch(url);
    if (!res.ok) {
      return { value: null, error: `HTTP ${res.status}` };
    }
    const data = await res.json();
    const result = data?.data?.result ?? [];
    if (!Array.isArray(result) || result.length === 0) {
      return { value: null, error: 'NoData' };
    }
    const v = Number(result[0]?.value?.[1]);
    return { value: Number.isFinite(v) ? v : null, error: Number.isFinite(v) ? null : 'NaN' };
  } catch (err) {
    return { value: null, error: String(err) };
  }
}

const metrics = {};
for (const [name, promql] of Object.entries(queries)) {
  const result = await queryOne(promql);
  metrics[name] = {
    query: promql,
    value: result.value,
    error: result.error,
  };
}

const dbCpuPrimaryResult = await queryOne(dbCpuPrimary);
if (dbCpuPrimaryResult.value != null) {
  metrics.db_cpu_pct = {
    query: dbCpuPrimary,
    value: dbCpuPrimaryResult.value,
    error: dbCpuPrimaryResult.error,
  };
} else {
  const dbCpuFallbackResult = await queryOne(dbCpuFallback);
  metrics.db_cpu_pct = {
    query: `${dbCpuPrimary} || ${dbCpuFallback}`,
    value: dbCpuFallbackResult.value,
    error: dbCpuFallbackResult.error ?? dbCpuPrimaryResult.error,
  };
}

const payload = {
  collected_at: new Date().toISOString(),
  base_url: baseUrl,
  metrics,
};

fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, `${JSON.stringify(payload, null, 2)}\n`, 'utf8');
console.log(`Prometheus snapshot written: ${output}`);
