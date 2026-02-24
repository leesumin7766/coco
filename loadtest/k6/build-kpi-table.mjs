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

function parseCsv(csvText) {
  const lines = csvText.trim().split(/\r?\n/);
  const headers = lines[0].split(',');
  return lines.slice(1).map((line) => {
    const cols = line.split(',');
    const row = {};
    headers.forEach((h, i) => {
      row[h] = cols[i];
    });
    return row;
  });
}

function loadScenarioK6(csvPath, scenario) {
  const rows = parseCsv(fs.readFileSync(csvPath, 'utf8'));
  const row = rows.find((r) => r.scenario === scenario);
  if (!row) throw new Error(`Scenario "${scenario}" not found in ${csvPath}`);
  return {
    p95: Number(row.p95_ms),
    p99: Number(row.p99_ms),
    rps: Number(row.rps),
    errorRatePct: Number(row.error_rate_pct),
  };
}

function loadProm(jsonPath) {
  const json = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
  const get = (name) => Number(json?.metrics?.[name]?.value ?? NaN);
  return {
    slowQueryPerSec: get('slow_query_per_sec'),
    hikariPendingMax: get('hikari_pending_max_5m'),
    dbCpuPct: get('db_cpu_pct'),
    redisHitRatioPct: get('redis_hit_ratio_pct'),
  };
}

function fmt(v) {
  return Number.isFinite(v) ? v.toFixed(3) : 'N/A';
}

function delta(before, after, betterWhenLower = true) {
  if (!Number.isFinite(before) || !Number.isFinite(after)) return 'N/A';
  const abs = after - before;
  const pct = before === 0 ? NaN : ((betterWhenLower ? before - after : after - before) / before) * 100;
  if (!Number.isFinite(pct)) return `${abs.toFixed(3)} (N/A)`;
  return `${abs.toFixed(3)} (${pct.toFixed(2)}%)`;
}

const args = parseArgs(process.argv);
const beforeK6 = args['before-k6'];
const afterK6 = args['after-k6'];
const beforeProm = args['before-prom'];
const afterProm = args['after-prom'];
const scenario = args.scenario || 'mixed';
const outMd = args['out-md'];
const outCsv = args['out-csv'];

if (!beforeK6 || !afterK6 || !beforeProm || !afterProm || !outMd || !outCsv) {
  console.error(
    'Usage: build-kpi-table.mjs --before-k6 a.csv --after-k6 b.csv --before-prom a.json --after-prom b.json --out-md kpi.md --out-csv kpi.csv [--scenario mixed]',
  );
  process.exit(1);
}

const bk6 = loadScenarioK6(beforeK6, scenario);
const ak6 = loadScenarioK6(afterK6, scenario);
const bProm = loadProm(beforeProm);
const aProm = loadProm(afterProm);

const rows = [
  { metric: 'API p95(ms)', before: bk6.p95, after: ak6.p95, betterWhenLower: true },
  { metric: 'API p99(ms)', before: bk6.p99, after: ak6.p99, betterWhenLower: true },
  { metric: 'RPS', before: bk6.rps, after: ak6.rps, betterWhenLower: false },
  { metric: 'SlowQuery/s', before: bProm.slowQueryPerSec, after: aProm.slowQueryPerSec, betterWhenLower: true },
  { metric: 'Hikari pending max', before: bProm.hikariPendingMax, after: aProm.hikariPendingMax, betterWhenLower: true },
  { metric: 'DB CPU%', before: bProm.dbCpuPct, after: aProm.dbCpuPct, betterWhenLower: true },
  { metric: 'Redis hit ratio%', before: bProm.redisHitRatioPct, after: aProm.redisHitRatioPct, betterWhenLower: false },
];

const csvLines = ['metric,before,after,delta_abs_and_pct'];
for (const row of rows) {
  csvLines.push(`${row.metric},${fmt(row.before)},${fmt(row.after)},"${delta(row.before, row.after, row.betterWhenLower)}"`);
}

const md = [
  `# KPI Table (${scenario} scenario)`,
  '',
  '| Metric | Before | After | Delta(abs/%) |',
  '|---|---:|---:|---:|',
  ...rows.map((row) => `| ${row.metric} | ${fmt(row.before)} | ${fmt(row.after)} | ${delta(row.before, row.after, row.betterWhenLower)} |`),
  '',
  '- Improvement formula (lower is better): `(Before - After) / Before * 100`',
  '- Improvement formula (higher is better): `(After - Before) / Before * 100`',
].join('\n');

fs.mkdirSync(path.dirname(outCsv), { recursive: true });
fs.mkdirSync(path.dirname(outMd), { recursive: true });
fs.writeFileSync(outCsv, `${csvLines.join('\n')}\n`, 'utf8');
fs.writeFileSync(outMd, `${md}\n`, 'utf8');

console.log(`KPI CSV written: ${outCsv}`);
console.log(`KPI Markdown written: ${outMd}`);
