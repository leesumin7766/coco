## 개요
`bidding-concurrency-lock` 브랜치의 main 대비 누적 변경 전체를 반영하는 PR입니다.

## 포함된 누적 변경(브랜치 전체)
### 1) 입찰 동시성 제어 개선
- `BiddingService#createBidding` 트랜잭션 경계 강화
- 매칭 후보 조회에 `PESSIMISTIC_WRITE` 잠금 적용
- 동시 요청 시 중복 체결/경합 위험 완화

### 2) 관측(요청 단위 기록) 기반 추가
- 요청 로그 엔티티/리포지토리/필터 추가
- trace id(`X-Request-Id`) 수집 및 응답 헤더 반영
- 요청 메서드/경로/상태코드/지연시간/사용자/클라이언트IP 저장

### 3) 감사로그 + 거래/결제 이벤트 이력 저장
- `audit_logs`, `payment_events`, `trade_events` 모델/저장 로직 추가
- 결제 확정/입찰 생성/매칭/취소 시 이력 적재

### 4) 시간/일 집계 지표 저장
- `metrics_hourly`, `metrics_daily` 집계 테이블 추가
- 스케줄러 기반 시간/일 집계 작업 추가
- `batch_job_meta` 갱신으로 배치 실행 상태 추적

### 5) DB/모니터링 구성 정비
- MariaDB slow query/performance schema 옵션 반영
- `mariadb-exporter` 추가 및 Prometheus scrape 타깃 연동
- exporter v0.18 호환 인증 설정(`--mysqld.username` + `MYSQLD_EXPORTER_PASSWORD`) 적용

### 6) Compose 환경변수화
- `docker-compose.yml` 내 비밀번호/시크릿/Redis 설정 하드코딩 제거
- 루트 `.env` 파일로 DB/JWT/Toss/Naver/Redis 변수 분리

## 주요 변경 파일
- `docker-compose.yml`
- `.env`
- `monitoring/prometheus.yml`
- `sql/init.sql`
- `yeezy/src/main/java/com/example/shop/service/BiddingService.java`
- `yeezy/src/main/java/com/example/shop/service/ConfirmPaymentService.java`
- `yeezy/src/main/java/com/example/shop/config/SchedulingConfig.java`
- `yeezy/src/main/java/com/example/shop/observability/**`

## 확인 사항
- compile 성공(`./gradlew compileJava`)
- 기존 `contextLoads` 테스트 환경 이슈는 별도(기존 상태 지속)
