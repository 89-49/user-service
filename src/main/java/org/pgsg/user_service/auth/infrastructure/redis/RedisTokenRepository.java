package org.pgsg.user_service.auth.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RT_PREFIX = "RT:";

    @Override
    public void saveRefreshToken(UUID userId, String refreshTokenHash, Duration duration) {
        redisTemplate.opsForValue().set(RT_PREFIX + userId, refreshTokenHash, duration);
    }

    @Override
    public Optional<String> findRefreshTokenHash(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(RT_PREFIX + userId));
    }

    @Override
    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(RT_PREFIX + userId);
    }

    @Override
    public boolean existsByRefreshTokenHash(String refreshTokenHash) {
        // Redis에서 값(value)만으로 키를 찾는 것은 성능상 좋지 않으므로
        // 우선 false를 반환하거나 필요 시 로직을 추가 예정.
        return false;
    }
}