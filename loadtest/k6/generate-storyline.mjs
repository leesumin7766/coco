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

function parseKpiCsv(csvPath) {
  const lines = fs.readFileSync(csvPath, 'utf8').trim().split(/\r?\n/).slice(1);
  const map = new Map();
  for (const line of lines) {
    const m = line.match(/^([^,]+),([^,]+),([^,]+),"(.*)"$/);
    if (!m) continue;
    map.set(m[1], { before: m[2], after: m[3], delta: m[4] });
  }
  return map;
}

const args = parseArgs(process.argv);
const kpiCsv = args.kpi;
const output = args.output;
const baselineDate = args['baseline-date'] || '2026-02-23';

if (!kpiCsv || !output) {
  console.error('Usage: generate-storyline.mjs --kpi <kpi.csv> --output <storyline.md> [--baseline-date 2026-02-23]');
  process.exit(1);
}

const kpi = parseKpiCsv(kpiCsv);
const read = (metric) => kpi.get(metric) || { before: 'N/A', after: 'N/A', delta: 'N/A' };

const p95 = read('API p95(ms)');
const p99 = read('API p99(ms)');
const rps = read('RPS');
const slowQ = read('SlowQuery/s');
const pending = read('Hikari pending max');
const dbCpu = read('DB CPU%');
const hit = read('Redis hit ratio%');

const doc = `# 모니터링 + Redis 성능개선 스토리라인 (15분)

## Slide 1. 도입 배경 (1.5분)
- 기준 시점: ${baselineDate} 이전.
- 문제: API 지연 시 원인을 애플리케이션/DB로 즉시 분리하기 어려웠음.
- 결과: 장애 대응 시간이 길어지고 재현 기반 개선이 어려웠음.

## Slide 2. 도입 이유 (1.5분)
- 모니터링: 병목 지점을 수치로 분리해서 빠르게 의사결정하기 위해.
- Redis 캐시: 반복 조회가 많은 \`/api/products/{id}\`의 DB 왕복을 줄이기 위해.

## Slide 3. 대안 비교 (2분)
- 로컬 캐시(Caffeine): 단순하지만 멀티 인스턴스 확장/일관성 한계.
- DB 인덱스만 강화: 필요하지만 hot key read offload에는 제한적.
- 현상 유지: 관측 부족과 지연 재발 리스크 유지.

## Slide 4. 실제 도입 내용 (2분)
- 모니터링: Prometheus + Grafana + Alert 규칙 + Slow Query(TABLE/FILE) 파이프라인.
- Redis 캐시: \`@Cacheable(productDetail)\`, TTL 10분, 장애 시 gate 우회 로직.

## Slide 5. 실험 설계 (1.5분)
- 동일 코드 + 동일 인프라에서 A/B 테스트:
  - Before: \`SPRING_CACHE_TYPE=none\`
  - After: \`SPRING_CACHE_TYPE=redis\`
- k6 시나리오: hot / mixed / cold, 각 3회 반복 중앙값.
- 대표값은 mixed 시나리오를 사용.

## Slide 6. 전/후 수치 결과 (2분)
| Metric | Before | After | Delta(abs/%) |
|---|---:|---:|---:|
| API p95(ms) | ${p95.before} | ${p95.after} | ${p95.delta} |
| API p99(ms) | ${p99.before} | ${p99.after} | ${p99.delta} |
| RPS | ${rps.before} | ${rps.after} | ${rps.delta} |
| SlowQuery/s | ${slowQ.before} | ${slowQ.after} | ${slowQ.delta} |
| Hikari pending max | ${pending.before} | ${pending.after} | ${pending.delta} |
| DB CPU% | ${dbCpu.before} | ${dbCpu.after} | ${dbCpu.delta} |
| Redis hit ratio% | ${hit.before} | ${hit.after} | ${hit.delta} |

## Slide 7. 결과 해석 (1.5분)
- API p95/p99 개선과 RPS 변화로 사용자 체감/처리량을 확인.
- SlowQuery/s, Hikari pending, DB CPU% 동반 개선으로 DB 부하 완화 확인.
- Redis hit ratio 증가로 캐시 전략 유효성 확인.

## Slide 8. 리스크/한계와 대응 (1.5분)
- 스탬피드: TTL 동시 만료 시 순간 DB 스파이크.
- stale 데이터: TTL 구간의 최신성 손실.
- p95 NoData: exporter/DB 버전 차이 가능.
- 대응: TTL 지터, selective evict, TABLE 기반 slow_log 백업 패널.

## Slide 9. 결론/다음 단계 (1분)
- 결론: 모니터링으로 병목 분리, Redis 캐시로 상세 조회 성능을 수치 기반 개선.
- 다음 단계:
  1. Redis 전용 Grafana 패널 추가(히트율/메모리/eviction)
  2. 캐시 무효화 정책 고도화(쓰기 경로 연동)
  3. CI에서 짧은 성능 스모크 자동화
`;

fs.mkdirSync(path.dirname(output), { recursive: true });
fs.writeFileSync(output, `${doc}\n`, 'utf8');
console.log(`Storyline script written: ${output}`);
