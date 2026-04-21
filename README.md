# user-service

MSA 환경에서 사용할 JWT 기반 인증 서버입니다.  
회원가입, 로그인, 토큰 발급/재발급/검증, 로그아웃 기능을 제공합니다.

---

## 구현 방향 개요

현재는 빠른 배포를 위해 회원 기능과 인증 기능을 하나의 서비스에서 제공하되, 나중에 `auth-service`로 분리할 가능성과 게이트웨이 도입을 고려해 
DDD를 기반으로 `user`, `auth` 패키지를 독립적으로 구성하여 기능 구현을 진행합니다.

### 향후 확장 방향

```
현재
user-service (auth + user 통합)

auth-service 분리 시
auth/infrastructure/UserReaderImpl -> UserFeignClient로 교체만 하면 됨

게이트웨이 도입 시
1. JwtAuthenticationFilter -> Gateway로 이전
   Gateway가 JWT 검증 + 블랙리스트 확인 후 헤더 변환
   Authorization: Bearer {token}
       -> X-User-Id: uuid
       -> X-User-Role: ROLE_USER
       -> ...

2. 각 MSA 서비스 컨트롤러 교체
   @AuthenticationPrincipal UUID userId
       -> @RequestHeader("X-User-Id") UUID userId

3. 보안 — MSA 서비스는 Gateway를 통해서만 접근 가능하도록
   외부 포트 바인딩 제거, 내부 네트워크만 허용
   (외부에서 X-User-Id 헤더 직접 조작한 인증 우회 방지)

4. GET /api/v1/auth/verify 제거
   현재는 게이트웨이가 없어 다른 MSA 서비스가 직접 호출하는 임시 엔드포인트
   게이트웨이 도입 시 Gateway가 JWT 검증을 대신하므로 불필요해짐
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

```env
# DB
DB_URL=jdbc:postgresql://db:5432/userdb
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# JWT — 256비트 이상 랜덤 문자열 사용 권장
JWT_SECRET=your-very-long-secret-key-here
JWT_ACCESS_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000
```

> **주의**: `JWT_SECRET`은 반드시 설정해야 합니다. 값이 없으면 애플리케이션이 시작되지 않습니다.

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

## 컨테이너 구성

| 서비스 | 이미지 | 포트    | 설명 |
|--------|--------|-------|------|
| user-service | 로컬 빌드 | 18099 | 인증 서버 |
| db | postgres:16 | 5432  | PostgreSQL |
| redis | redis:7.2 | 6379  | RefreshToken 저장소 |

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