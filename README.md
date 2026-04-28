# user-service

MSA 환경에서 사용할 JWT 기반 인증 서버입니다.  
회원가입, 로그인, 토큰 발급/재발급/검증, 로그아웃 기능을 제공합니다.

---

## 구현 방향 개요

현재는 빠른 배포를 위해 회원 기능과 인증 기능을 하나의 서비스에서 제공하되, 나중에 `auth-service`로 분리할 가능성과 게이트웨이 도입을 고려해
DDD를 기반으로 `user`, `auth` 패키지를 독립적으로 구성하여 기능 구현을 진행합니다.

### 패키지 구조

<details>

<summary>패키지 구조 상세(구현 과정에서 다소 변경될 가능성 존재)</summary>

```
src/main/java/com/example/userservice/
├── auth/                               # 나중에 auth-service로 분리
│   ├── presentation/
│   │   └── AuthController.java
|   |       # POST /api/v1/auth 회원가입
│   │       # POST /api/v1/auth/login 로그인
│   │       # POST /api/v1/auth/logout 로그아웃
│   │       # POST /api/v1/auth/reissue 토큰 재발급
│   │       # POST /api/v1/auth/verify  <- 게이트웨이 도입 시 제거
│   ├── application/
│   │   ├── AuthService.java
│   │   └── dto/
│   ├── domain/
│   │   ├── JwtTokenProvider.java       # 도메인 서비스 인터페이스
│   │   ├── TokenRepository.java        # 포트 (인터페이스)
│   │   └── vo/
│   │       └── TokenPair.java          # accessToken + refreshToken VO
│   └── infrastructure/
│       ├── JwtTokenProviderImpl.java   # JwtTokenProvider 구현체 (jjwt)
│       ├── RedisTokenRepository.java   # TokenRepository 구현체 (Redis)
│       ├── UserDetailsServiceImpl.java # UserDetailsService 구현체
│       │   # 현재: UserRepository 직접 조회
│       │   # 분리 후: UserFeignClient로 교체
│       ├── UserDetailsImpl.java        # UserDetails 구현체
│       └── JwtProperties.java
│
├── user/                               # 회원 도메인
│   ├── presentation/
│   │   └── UserController.java
│   │       # POST /api/v1/users/signup
│   ├── application/
│   │   ├── UserService.java
│   │   └── dto/
│   ├── domain/
│   │   ├── User.java                   # 애그리거트 루트
│   │   ├── UserRole.java               # enum VO
│   │   └── UserRepository.java         # 포트 (인터페이스)
│   └── infrastructure/
│       └── UserRepositoryImpl.java     # UserRepository 구현체 (JPA)
│
└── global/
    ├── config/
    │   └── SecurityConfig.java
    ├── filter/
    │   └── JwtAuthenticationFilter.java  # 게이트웨이 도입 시 Gateway로 이전
    ├── security/
    │   └── JwtAuthenticationEntryPoint.java
    └── exception/
        └── GlobalExceptionHandler.java
```

</details>

### 향후 확장 방향

```
현재
user-service (auth + user 통합)
클라이언트 -> user-service (로그인, JWT 발급)
다른 MSA 서비스 -> POST /api/v1/auth/verify 호출로 검증 위임

auth-service 분리 시
auth/infrastructure/UserDetailsServiceImpl
    -> UserFeignClient로 교체 (user-service HTTP 호출)

게이트웨이 도입 시
1. 인증 흐름 변경
   클라이언트 -> Gateway (JWT 검증 + 블랙리스트 확인)
       -> X-User-Id, X-User-Role 헤더로 변환 후 각 MSA 서비스로 전달
   JWT 검증은 Gateway에서만 수행 (각 MSA 서비스는 헤더만 신뢰)

2. JwtAuthenticationFilter -> Gateway로 이전
   (예시) 각 MSA 서비스: @AuthenticationPrincipal -> @RequestHeader("X-User-Id") 교체

3. POST /api/v1/auth/verify 제거
   Gateway가 JWT 검증을 대신하므로 불필요해짐

4. 보안 — MSA 서비스는 Gateway를 통해서만 접근 가능하도록
   외부 포트 바인딩 제거, 내부 네트워크만 허용
   (외부에서 X-User-Id 헤더 직접 조작을 통한 인증 우회 방지)
```

---

## 기술 스택

- Java 21
- Spring Boot 3.3
- Spring Security
- PostgreSQL 17
- Redis 7.2
- JWT (jjwt 0.12.6)

---

## 사전 요구사항

- Docker Desktop 설치
- Docker Compose v2 이상

---

## 로컬 실행 방법

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd user-service
```

### 2. 환경변수 파일 생성

프로젝트 루트에 `.env` 파일을 생성합니다.

```bash
cp .env.example .env
```

`.env` 파일을 열어 아래 항목을 실제 값으로 수정합니다.

<details>

<summary> user-service 실행용 환경변수 설정(.env.example 파일)</summary>

```env
# DB
DB_USERNAME=postgres
DB_PASSWORD=your_password

# 로컬 실행용(로컬, 배포용 설정 중 하나는 주석 처리할 것)
DB_URL=jdbc:postgresql://localhost:5432/userdb

# docker 기반 배포용
DB_URL=jdbc:postgresql://db:5432/userdb

# Redis
REDIS_PORT=6379
# 로컬 실행용(로컬, 배포용 설정 중 하나는 주석 처리할 것)
REDIS_HOST=localhost

# docker 기반 배포용
REDIS_HOST=redis


# JWT — 256비트 이상 랜덤 문자열 권장
JWT_SECRET=CHANGE_ME_BASE64_32_BYTES_MINIMUM_STRING
JWT_ACCESS_TOKEN_EXPIRATION=1800000
JWT_REFRESH_TOKEN_EXPIRATION=604800000


# Config-Server
CONFIG_SERVER=localhost:13100


# JPA 설정(배포 환경에서 초기 구동 완료 직후 환경변수값 수정 필요)
# 배포 시 ddl-auto는 validate, show-sql은 false로 변경
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true


# server port
SERVER_PORT=도메인_서비스별_포트번호


# 배포 환경에서도 공통 모듈을 적용하기 위해 Dockerfile에 추가해야 할 환경변수
GPR_USER=GitHub_ID
GPR_TOKEN=GitHub_Personal_Access_Token(PAT)


# eureka server 초기 주소
# 로컬 실행용(로컬, 배포용 설정 중 하나는 주석 처리할 것)
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/

# docker 기반 실행용(배포용)
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# kafka 설정(로컬용)
KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092
```

</details>


> **주의**: `JWT_SECRET`은 반드시 설정해야 합니다. 값이 없으면 애플리케이션이 시작되지 않습니다.
> 일부 환경변수의 경우, 로컬용과 배포용 값이 별도로 구분됩니다.

### 3. Docker Compose 실행

```bash
docker compose up --build
```

최초 실행 시 이미지 빌드가 포함되므로 수 분이 소요될 수 있습니다.  
이후 실행부터는 아래 명령어로 빠르게 시작할 수 있습니다.

```bash
docker compose up
```

---

## 배포 환경 실행 및 설정 조정 (Deployment & Overrides)

원격 서버에 배포했을 경우에는 보안 및 데이터 보호를 위해 `.env` 파일의 내용을 직접 수정하지 않고, 실행 시점에 환경 변수를 주입하여 설정을 조정하는 방식을 권장합니다.

### 1. 주요 조정 항목 (핵심 환경변수)
배포 시 주로 변경하게 되는 주요 설정입니다:
- `SERVER_PORT`: 서비스 포트 번호 (예: `8080`)
- `DB_URL`: 배포용 DB 연결 정보 (예: `jdbc:postgresql://db:5432/userdb` - 포트 변경 시 `5432` 수정)
- `REDIS_HOST`: 배포용 Redis 서버 주소 (예: `redis`)
- `REDIS_PORT`: Redis 포트 번호 (예: `6379`)
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: 유레카 서버 주소
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: `validate` 또는 `none` (데이터 유실 방지)
- `SPRING_JPA_SHOW_SQL`: `false` (로그 최적화)

### 2. Docker Compose로 실행 (가장 간편함)
기본 `.env`의 설정은 유지하면서, 특정 값만 덮어씌워 한 번에 빌드 및 실행하는 명령어입니다.

```bash
SERVER_PORT=8080 \
DB_URL=jdbc:postgresql://db:5432/userdb \
REDIS_HOST=redis \
REDIS_PORT=6379 \
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/ \
SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
SPRING_JPA_SHOW_SQL=false \
docker compose up -d --build
```

### 3. Docker 개별 명령어로 실행 (개별 컨테이너 관리 시)
`.env` 파일에 GitHub 자격 증명이 포함된 경우, 아래 순서대로 실행합니다.

```bash
# 1. 시크릿을 참조하여 이미지 빌드
export $(grep -v '^#' .env | xargs)
docker build --secret id=GPR_USER,env=GPR_USER --secret id=GPR_TOKEN,env=GPR_TOKEN -t user-service .

# 2. 배포용 설정을 주입하여 컨테이너 실행
# [주의] -e SERVER_PORT와 -p의 내부 포트 번호(오른쪽)는 반드시 일치해야 합니다.
docker run -d \
  --name user-service \
  --env-file .env \
  -e SERVER_PORT=8080 \    # 내부 앱이 사용할 포트 지정
  -e DB_URL=jdbc:postgresql://db:5432/userdb \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/ \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  -e SPRING_JPA_SHOW_SQL=false \
  -p 8080:8080 \           # 외부포트:내부포트 매핑 (내부 포트는 위 SERVER_PORT와 일치)
  --network pgsg-network \
  user-service
```
*※ `-e SERVER_PORT` 옵션은 `application.yml`의 기본 설정을 덮어쓰며, `-p` 옵션과 함께 사용하여 외부 접속 경로를 완성합니다.*
*※ `-e` 옵션은 `--env-file`보다 우선순위가 높아 파일 수정 없이도 설정 변경이 가능합니다.*

---

## 컨테이너 구성

| 서비스          | 이미지         | 포트    | 설명               |
|--------------|-------------|-------|------------------|
| user-service | 로컬 빌드       | 18099 | 인증 서버            |
| db           | postgres:17 | 5432  | PostgreSQL       |
| redis        | redis:7.2   | 6379  | RefreshToken 저장소 |

---

## 주요 명령어

```bash
# 전체 컨테이너 시작 (백그라운드)
docker compose up -d

# 로그 확인
docker compose logs -f user-service

# 전체 컨테이너 중지
docker compose down

# 컨테이너 + 볼륨 삭제 (DB 데이터 초기화)
docker compose down -v

# 이미지 재빌드 후 시작
docker compose up --build
```
