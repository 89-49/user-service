# 🛡️ PGSG User Service (Authentication & Member Management)

본 프로젝트는 MSA(Microservice Architecture) 환경에서 보안과 확장을 책임지는 **JWT 기반 통합 인증 및 회원 관리 서버**입니다.  
고성능 회원 조회, 안전한 인증 흐름, 그리고 이벤트 기반 아키텍처를 지원하기 위한 기술적 장치들이 포함되어 있습니다.

---

## 🏗️  패키지 구조 및 레이어링

```
src/main/java/org/pgsg/user_service/
├── auth/                            # 인증 도메인
│   ├── application/                 # 서비스(AuthService) 및 토큰 발행 로직
│   ├── domain/                      # 인증 정책 인터페이스 및 값 객체
│   ├── infrastructure/              # RedisTokenRepository, JwtTokenProvider, Security 필터
│   └── presentation/                # AuthController (로그인, 회원가입 API)
│
├── user/                            # 회원 도메인
│   ├── application/                 # Facade(조율) 및 도메인 서비스
│   ├── domain/                      # User 엔티티, UserRole, UserRepository 인터페이스
│   ├── infrastructure/              # JpaUserRepository, UserQueryRepository(QueryDSL)
│   └── presentation/                # UserController (회원 정보 관리 API)
│
└── UserServiceApplication.java      # Application Bootstrapping
```

---

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 | 상세 |
| :--- | :--- | :--- |
| **Framework** | Spring Boot 3.5.13 | Java 21 LTS 기반 |
| **Database** | PostgreSQL 17 | UUID 및 JSONB 지원 |
| **Cache** | Redis 7.2 | RefreshToken 저장 및 분산 락 용도 |
| **Persistence** | Spring Data JPA / QueryDSL | Type-safe한 쿼리 작성 및 성능 최적화 |
| **Messaging** | Apache Kafka | Microservice 간 비동기 이벤트 전송 (Outbox) |
| **Security** | Spring Security / JWT | Stateless 인증 및 인가 |
| **Infrastructure** | Spring Cloud | Eureka(Discovery), Config(Centralized Config) |

---

## ⚙️ 설정 관리 및 운영 전략

### 1. 로컬 설정 최적화 및 우선순위 (`application.yml`)
중앙 설정 서버(Config Server)를 통해 공통 정책을 관리하되, 로컬 개발 및 Docker 환경의 유연성을 위해 **로컬 환경 변수가 원격 설정을 덮어쓰도록(Override)** 구성하였습니다.

<details>
<summary> 📄 application.yml 로컬 설정 전문 (펼치기)</summary>

```yaml
server:
  port: ${SERVER_PORT:18099}

spring:
  application:
    name: user-service

  profiles:
    active: dev, kafka
    include: error, user-error

  # [필수 1] 설정 로드 순서 (중요)
  # .env를 먼저 읽어야 CONFIG_SERVER 주소를 확보하여 접속할 수 있습니다.
  config:
    import:
      - "optional:file:.env[.properties]"
      - "optional:configserver:${CONFIG_SERVER:http://34.50.50.170:13100}"

  # [필수 2] 로컬 우선순위 설정
  # 로컬 .env나 환경변수가 원격 Config Server의 값을 덮어쓰도록 허용합니다.
  cloud:
    config:
      allow-override: true
      override-none: true
      override-system-properties: false
      discovery:
        service-id: config-server

  # [필수 3] 빈 중복 정의 허용
  main:
    allow-bean-definition-overriding: true

# [필수 4] 보안 설정 (JWT)
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:1800000}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

eureka:
  instance:
    instance-id: ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}
```
</details>

### 2. 원격 중앙 설정 (`user-service/application.yml`)
Config Server의 Git 리포지토리에서 관리되는 공통 및 인프라 구조 설정입니다. 실제 값은 환경 변수(`${...}`)를 통해 주입받도록 설계되어 있습니다.

<details>
<summary> 📄 원격 application.yml 설정 전문 (펼치기)</summary>

```yaml
server:
  port: ${SERVER_PORT:8081}

spring:
  application:
    name: user-service

  main:
    allow-bean-definition-overriding: true

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/userdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}
    show_sql: ${SPRING_JPA_SHOW_SQL:false}
    defer-datasource-initialization: true

  sql:
    init:
      mode: always

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:1800000}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:604800000}
```
</details>

### 3. Kafka SSL 볼륨 마운트 (Troubleshooting)
Java 보안 라이브러리의 제약으로 인해 SSL 트러스트스토어(`jks`)는 JAR 내부가 아닌 **물리 파일 시스템**에 위치해야 합니다.
- **해결책**: `docker-compose.yml` 볼륨 설정을 통해 호스트의 `./ssl` 폴더를 컨테이너 `/app/ssl`로 마운트하여 기동 시 즉시 로드 가능하게 처리했습니다.

### 3. 인프라 구성 정보 (Docker Configuration)
본 프로젝트는 보안을 위해 민감 정보(비밀번호, 토큰 등)를 파일에 직접 기록하지 않고, Docker Secrets 및 환경 변수 시스템을 활용합니다.

<details>
<summary> 🐳 Dockerfile (멀티 스테이지 빌드)</summary>

```dockerfile
# 빌드 스테이지: GitHub 패키지 접근을 위해 시크릿 마운트 사용
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=GPR_USER \
    --mount=type=secret,id=GPR_TOKEN \
    export GPR_USER=$(cat /run/secrets/GPR_USER) && \
    export GPR_TOKEN=$(cat /run/secrets/GPR_TOKEN) && \
    gradle bootJar --no-daemon

# 실행 스테이지: 경량화된 JRE 환경
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
</details>

<details>
<summary> 🐙 docker-compose.yml (서비스 오케스트레이션)</summary>

```yaml
services:
  user-service:
    container_name: user-service
    build:
      context: .
      secrets:
        - GPR_USER
        - GPR_TOKEN
    environment:
      - "SERVER_PORT=${SERVER_PORT:-18099}"
    ports:
      - "${SERVER_PORT:-18099}:${SERVER_PORT:-18099}"
    env_file:
      - .env
    volumes:
      - ./src/main/resources/ssl:/app/ssl
    depends_on:
      db: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - pgsg-network

  db:
    image: postgres:17
    environment:
      POSTGRES_DB: ${DB_NAME:-userdb}
      POSTGRES_USER: ${DB_USERNAME:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME:-postgres}"]
    networks:
      - pgsg-network

  redis:
    image: redis:7.2
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
    networks:
      - pgsg-network

networks:
  pgsg-network:
    name: pgsg-network
    external: true

volumes:
  postgres-data:

secrets:
  GPR_USER: { environment: GPR_USER }
  GPR_TOKEN: { environment: GPR_TOKEN }
```
</details>

---

원격 서버 배포 시 보안 및 데이터 보호를 위해 `.env` 파일을 수정하는 대신, 컨테이너 실행 시점에 환경 변수를 주입하여 설정을 동적으로 조정하는 방식을 권장합니다.

### 1. 주요 조정 항목 (핵심 환경변수)
- `SERVER_PORT`: 서비스 포트 번호 (예: `8081`)
- `DB_URL`: 운영 DB 주소 (예: `jdbc:postgresql://prod-db:5432/userdb`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: `validate` (운영 환경 필수)
- `EUREKA_SERVER_URL`: 운영 유레카 서버 주소

### 2. Docker Compose를 이용한 동적 주입
기본 설정을 유지하며 특정 값만 덮어씌워 실행하는 가장 간편한 방식입니다.
```bash
SERVER_PORT=8081 \
SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
SPRING_JPA_SHOW_SQL=false \
docker compose up -d --build
```

### 3. Docker 개별 명령어로 실행
개별 컨테이너 단위로 관리하거나 오케스트레이션 도구 없이 실행할 때 사용합니다.
```bash
docker run -d \
  --name user-service \
  --env-file .env \
  -e SERVER_PORT=8081 \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  -p 8081:8081 \
  --network pgsg-network \
  user-service
```

---

## 🚦 시작하기 (Getting Started)

### 1. 환경 변수 구성 (`.env`)
프로젝트 루트에 `.env` 파일을 생성하고, 최신화된 `.env.example` 내용을 참고하여 실제 환경에 맞는 값을 작성합니다.

```env
# [Database] PostgreSQL 설정
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
DB_URL=jdbc:postgresql://db:5432/userdb

# [Redis] 토큰 저장소 설정
REDIS_HOST=redis
REDIS_PORT=6379

# [JWT] 인증 보안 설정 (256비트 이상 랜덤 문자열 권장)
JWT_SECRET=YOUR_SECURE_BASE64_ENCODED_SECRET
JWT_ACCESS_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000

# [MSA Infrastructure] 중앙 설정 및 서비스 디스커버리
CONFIG_SERVER=http://your-config-server-ip:13100
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

# [Application] 서버 구동 설정
SERVER_PORT=18099
HOSTNAME=user-service

# [JPA] 데이터베이스 초기화 전략
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true

# [GitHub Packages] 공통 모듈 빌드 자격 증명 (PAT)
GPR_USER=your_github_id
GPR_TOKEN=your_personal_access_token

# [Kafka] 이벤트 메시징 및 SSL 설정
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SPRING_KAFKA_SSL_TRUST_STORE_LOCATION=/app/ssl/kafka.server.truststore.jks
TRUST_STORE_PASSWORD=your_truststore_password
```

### 2. Docker를 이용한 실행
```bash
# 외부 통신을 위한 네트워크 생성
docker network create pgsg-network

# 빌드 및 백그라운드 실행
docker compose up -d --build
```

---

## 📮 주요 API 가이드

### 1. 인증 및 권한 관련 (Auth API)
| 기능 | 메서드 | 경로 | 권한 | 설명 |
| :--- | :---: | :--- | :--- | :--- |
| **로그인** | `POST` | `/api/v1/auth/login` | 누구나 | 사용자 인증 및 토큰 쌍(Access/Refresh) 발급 |
| **회원가입** | `POST` | `/api/v1/auth/signup` | 누구나 | 신규 사용자 등록 |
| **로그아웃** | `POST` | `/api/v1/auth/logout` | 인증 사용자 | Access Token 블랙리스트 처리 및 로그아웃 |
| **토큰 재발급** | `POST` | `/api/v1/auth/reissue` | 누구나 | Refresh Token을 이용한 Access Token 갱신 |

### 2. 회원 정보 관리 (User API)
| 기능 | 메서드 | 경로 | 권한 | 설명 |
| :--- | :---: | :--- | :--- | :--- |
| **내 정보 조회** | `GET` | `/api/v1/users/me` | 인증 사용자 | 현재 로그인된 사용자의 상세 프로필 조회 |
| **회원 상세 조회** | `GET` | `/api/v1/users/{userId}` | 인증 사용자 | 특정 사용자의 상세 정보 조회 (권한 확인 포함) |
| **회원 목록 검색** | `GET` | `/api/v1/users` | 관리자 | QueryDSL 기반 동적 검색 및 페이징 조회 |
| **내 프로필 수정** | `PATCH` | `/api/v1/users/me` | 인증 사용자 | 본인의 닉네임, 이름 등 프로필 정보 수정 |
| **회원 정보 수정** | `PATCH` | `/api/v1/users/{userId}` | 관리자 | 관리자 권한으로 특정 회원 정보(역할 등) 수정 |
| **회원 탈퇴/삭제** | `DELETE` | `/api/v1/users/{userId}` | 인증/관리자 | 회원 계정 삭제 (Soft Delete 권장) |

### 3. 서비스 간 통신용 (Internal API)
| 기능 | 메서드 | 경로 | 설명 |
| :--- | :---: | :--- | :--- |
| **토큰 검증** | `POST` | `/internal/v1/auth/verify` | 게이트웨이 등에서 토큰의 블랙리스트 여부 확인 |
| **인증용 정보 조회** | `GET` | `/internal/v1/users` | username으로 인증에 필요한 최소 정보 조회 |
| **회원 상세 연동** | `GET` | `/internal/v1/users/{userId}` | 타 서비스에서 UUID로 회원 정보 연동 시 사용 |

---

## 📈 모니터링 및 관리
- **Actuator**: `/actuator/health`, `/actuator/info`를 통해 서비스 상태 확인 가능.
- **Log 관리**: MDC 필터를 적용하여 요청별 추적 ID(Trace ID)를 로그에 남겨 MSA 분산 추적 지원.
