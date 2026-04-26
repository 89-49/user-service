# 빌드 스테이지
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# GitHub Packages 인증을 위한 인자 추가
ARG GPR_USER
ARG GPR_TOKEN
ENV GPR_USER=$GPR_USER
ENV GPR_TOKEN=$GPR_TOKEN

COPY . .
RUN gradle bootJar --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]