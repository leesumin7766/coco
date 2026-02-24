import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://backend:8080').replace(/\/+$/, '');
const SCENARIO = (__ENV.SCENARIO || 'hot').toLowerCase();
const HOT_ID = (__ENV.HOT_ID || '1').trim();
const PRODUCT_IDS = (__ENV.PRODUCT_IDS || '1,2,3,4,5')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const SLEEP_MS = Number(__ENV.SLEEP_MS || '0');

export const options = {
  vus: Number(__ENV.VUS || '30'),
  duration: __ENV.DURATION || '60s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

const businessErrors = new Counter('business_errors');

function pickProductId() {
  if (PRODUCT_IDS.length === 0) return HOT_ID;

  if (SCENARIO === 'hot') return HOT_ID;
  if (SCENARIO === 'mixed') return Math.random() < 0.8
    ? HOT_ID
    : PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
  if (SCENARIO === 'cold') return PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];

  return HOT_ID;
}

export default function () {
  const productId = pickProductId();
  const res = http.get(`${BASE_URL}/api/products/${productId}`, {
    tags: {
      endpoint: 'product-detail',
      scenario: SCENARIO,
    },
  });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
    'response body exists': (r) => typeof r.body === 'string' && r.body.length > 2,
  });

  if (!ok) {
    businessErrors.add(1);
  }

  if (SLEEP_MS > 0) {
    sleep(SLEEP_MS / 1000);
  }
}
