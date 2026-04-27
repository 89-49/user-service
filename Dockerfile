# 빌드 스테이지
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# 빌드 시점에만 비밀 정보를 안전하게 마운트하여 사용 (Image History에 남지 않음)
COPY . .

RUN --mount=type=secret,id=GPR_USER \
    --mount=type=secret,id=GPR_TOKEN \
    export GPR_USER=$(cat /run/secrets/GPR_USER) && \
    export GPR_TOKEN=$(cat /run/secrets/GPR_TOKEN) && \
    gradle bootJar --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
