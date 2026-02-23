import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '5m', target: 20 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';

export default function () {
  const resBrands = http.get(`${BASE_URL}/api/brands`);
  check(resBrands, { 'brands status 200': (r) => r.status === 200 });

  const resSearch = http.get(`${BASE_URL}/api/products/search?query=air`);
  check(resSearch, { 'search status 200': (r) => r.status === 200 });

  sleep(1);
}
