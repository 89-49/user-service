# 빌드 스테이지
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]