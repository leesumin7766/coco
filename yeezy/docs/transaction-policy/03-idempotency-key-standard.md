# 멱등키(Idempotency Key) 표준

## 목적
중복 요청/재시도/네트워크 오류 상황에서 중복 데이터 적재를 방지하고, 동일 요청에 대해 동일 결과를 보장한다.

## 기본 원칙
- PG 멱등 정책은 PG 기준을 따름
- 내부 이벤트 멱등은 애플리케이션 + DB UNIQUE 제약으로 보장
- 키 포맷: `prefix + 업무키 + UUID`

## 표준 포맷
- 거래 체결 이벤트
  - `trade:{businessKey}:{uuid}`
  - `businessKey` 예시: `{productSizeId}:{buyBidId}:{sellBidId}`
- 결제 이벤트
  - `payment:{orderId}:{paymentKey}:{uuid}`
  - `paymentKey` 미사용 단계에서는 `payment:{orderId}:{uuid}` 허용

## 길이 정책
- DB 컬럼 길이: `VARCHAR(200)`
- 키 길이가 200자를 초과하면 업무키 구간을 해시로 축약
  - 예: `trade:{md5(businessKey)}:{uuid}`

## 생성 시점
- 요청 수신 직후(비즈니스 상태 변경 전) 생성
- 생성된 키를 트랜잭션 내 이벤트 row에 저장
- 동일 키 재요청 시 신규 처리 금지(기존 결과 반환)

## 저장 대상
- `trade_events.idempotency_key` (NOT NULL, UNIQUE)
- `payment_events.idempotency_key` (NOT NULL, UNIQUE 또는 조회 패턴 기반 제약)

## 제약/인덱스 원칙
- 멱등키는 단독 UNIQUE 우선
- 복합 UNIQUE는 실제 조회/무결성 요구가 확실한 경우만 추가
- 적용 전 점검 SQL

```sql
SELECT COUNT(*) AS null_cnt
FROM payment_events
WHERE idempotency_key IS NULL;

SELECT idempotency_key, COUNT(*)
FROM payment_events
GROUP BY idempotency_key
HAVING COUNT(*) > 1;
```

## 재시도 정책
- 멱등키 충돌 시 DB 에러를 비즈니스 에러로 매핑하지 않고 "기존 처리 결과" 조회 경로로 분기
- 결과 조회 우선순위
  1. 동일 멱등키 이벤트 row
  2. 관련 주문/체결 상태
  3. 없으면 재처리(정책에 따라)
