import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 50 },
    { duration: '10m', target: 50 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1200'],
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
