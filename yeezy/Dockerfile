# ── backend/Dockerfile ──────────────────────────────────────────
# 1) Build Stage
FROM gradle:jdk21-corretto AS builder
WORKDIR /app

# 캐시 가능한 파일만 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/
# gradlew 실행 권한 추가
RUN chmod +x gradlew

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon

# 나머지 소스 복사 & 빌드
COPY src/ src/
RUN ./gradlew clean build -x test --no-daemon

# 2) Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드된 JAR(Gradle 기본 출력: build/libs/*.jar) 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
