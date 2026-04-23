package org.pgsg.user_service.auth.domain;

import java.util.Optional;
import java.time.Duration;
import java.util.UUID;

public interface TokenRepository {

    // 토큰 저장
    void saveRefreshToken(UUID userId, String refreshTokenHash, Duration duration);

    // 저장된 토큰 조회
    Optional<String> findRefreshTokenHash(UUID userId);

    // 로그아웃 또는 토큰 갱신 시 삭제
    void deleteRefreshToken(UUID userId);

    // 특정 토큰 존재 여부 확인
    boolean existsByRefreshTokenHash(String refreshTokenHash);
}
