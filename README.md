# coco
07-14 user/coco 내의 폴더 사용할 것. 이 repo 사용할 것

> Spring Boot 3.5.3 / Java 21 / MariaDB / Redis / React 19 / OAuth2 기반  
> 대량 API 요청 대응, 동시성 제어, JPA 최적화, 캐시 전략을 중심으로 한 실전 실습 프로젝트입니다.

---

## ✅ 1. 느린 쿼리 단축 → **JPA + Query 최적화 실습**

### 📌 구현 기능
- 대용량 게시판 / 검색 서비스
- 조건별 필터링 + 정렬 + 페이징 처리

### 🧪 실습 내용
- `@EntityGraph`, `fetch join`을 통한 N+1 문제 해결
- Lazy → Eager 전략 변경 실험
- 불필요한 JOIN 제거 및 인덱스 설계
- 쿼리 로그 분석 (`spring.jpa.show-sql`, `hibernate.format_sql`)
- MySQL `slow_query_log`, `EXPLAIN` 분석

### 📊 성능 개선 결과
- **Before**: 2000건 조회 시 응답 1초 이상, CPU 사용률 80%
- **After**: 0.1초 응답, CPU 사용률 30%로 감소

---

## ✅ 2. 트랜잭션과 동시성 이슈 해결 → **주문/결제/재고 시스템**

### 📌 구현 기능
- 재고 감소 처리 (`@Transactional`, Lock 전략 적용)
- 동시 결제 처리 (Redis + Lua Script + Redisson 활용)

### 🧪 실습 내용
- `@Transactional` 동작 방식 심화 실습
- 동시성 제어 방식 비교 (`synchronized`, DB Lock, Redis Lock)
- Pessimistic vs Optimistic Lock 실험
- 재고 oversell 방지 시나리오 검증

### 📊 동시성 처리 결과
- **Before**: 1000명 동시 요청 중 재고 오류 10건 이상
- **After**: Redisson Lock 적용 후 오류 0건

---

## ✅ 3. 서버 최적화 및 병목 해소 → **대량 API 요청 처리 실습**

### 📌 구현 기능
- 인기 게시글 조회 API (트래픽 폭주 상황 가정)
- 비동기 처리 + 캐시 + 로드 밸런싱 구조 설계

### 🧪 실습 내용
- `@Async`, `CompletableFuture`, `ExecutorService`로 비동기 처리
- `Spring Actuator`, JVM Heap, Thread Dump로 병목 분석
- 로그 수집 및 모니터링 (`ELK`, `Prometheus`, `Grafana`)

### 📊 성능 개선 결과
- **Before**: TPS 50, 응답시간 평균 2.4초
- **After**: TPS 300, 응답시간 300ms로 개선

---

## ✅ 4. Redis 캐시 활용 → **캐시 전략 적용 프로젝트**

### 📌 구현 기능
- 인기 게시글 / 사용자 세션 / 검색 결과 캐싱

### 🧪 실습 내용
- `@Cacheable`, `@CacheEvict`, `RedisTemplate`, TTL 적용
- 캐시 무효화 전략 (LRU, TTL, 조건부 Eviction)
- Redis Pub/Sub 및 Lua 스크립트 실험

### 📊 캐시 도입 효과
- **Before**: DB 부하로 응답 1.5초
- **After**: Redis 캐시로 DB 호출 80% 감소, 응답 200ms로 단축

---

## 🔧 주요 기술 스택

| 분류         | 사용 기술 |
|--------------|-----------|
| Backend      | Spring Boot 3.5.3, JPA, Spring Security, OAuth2 |
| DB / 캐시     | MariaDB, Redis, Redisson, Lua Script |
| Concurrency  | `@Transactional`, Pessimistic/Optimistic Lock |
| Async / TPS  | `@Async`, CompletableFuture, Actuator, Grafana |
| Frontend     | React 19.1.0 |
| Monitoring   | Spring Actuator, Prometheus, ELK, Grafana |
| Tools        | DBeaver 25.1.2, Postman |
