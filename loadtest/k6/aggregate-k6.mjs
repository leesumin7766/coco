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

function median(values) {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0 ? (sorted[mid - 1] + sorted[mid]) / 2 : sorted[mid];
}

function readSummary(filePath) {
  const raw = fs.readFileSync(filePath, 'utf8');
  const json = JSON.parse(raw);

  const durationMetric = json?.metrics?.http_req_duration ?? {};
  const reqMetric = json?.metrics?.http_reqs ?? {};
  const failedMetric = json?.metrics?.http_req_failed ?? {};
  const duration = durationMetric.values ?? durationMetric;
  const reqs = reqMetric.values ?? reqMetric;
  const failed = failedMetric.values ?? failedMetric;

  return {
    p95: Number(duration['p(95)'] ?? NaN),
    p99: Number(duration['p(99)'] ?? NaN),
    rps: Number(reqs.rate ?? reqs.value ?? NaN),
    errorRatePct: Number(failed.rate ?? failed.value ?? NaN) * 100,
  };
}

function collectScenario(inputDir, scenario) {
  const dir = path.join(inputDir, scenario);
  if (!fs.existsSync(dir)) return [];
  return fs
    .readdirSync(dir)
    .filter((name) => name.endsWith('-summary.json'))
    .map((name) => readSummary(path.join(dir, name)))
    .filter((row) => Number.isFinite(row.p95) && Number.isFinite(row.p99) && Number.isFinite(row.rps));
}

function fmt(v) {
  return Number.isFinite(v) ? v.toFixed(3) : '';
}

const args = parseArgs(process.argv);
const inputDir = args.input;
const output = args.output || path.join(inputDir || '.', 'k6-median.csv');

if (!inputDir) {
  console.error('Usage: aggregate-k6.mjs --input <mode-dir> [--output <csv-path>]');
  process.exit(1);
}

const scenarios = ['hot', 'mixed', 'cold'];
const lines = ['scenario,run_count,p95_ms,p99_ms,rps,error_rate_pct'];

for (const scenario of scenarios) {
  const rows = collectScenario(inputDir, scenario);
  lines.push(
    [
      scenario,
      rows.length,
      fmt(median(rows.map((r) => r.p95))),
      fmt(median(rows.map((r) => r.p99))),
      fmt(median(rows.map((r) => r.rps))),
      fmt(median(rows.map((r) => r.errorRatePct))),
    ].join(','),
  );
}

fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, `${lines.join('\n')}\n`, 'utf8');
console.log(`k6 median CSV written: ${output}`);
