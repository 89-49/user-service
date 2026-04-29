package org.pgsg.user_service.auth.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

// TODO: user-service 내부에 유지 -> 토큰 저장용 redis는 오직 user-service만 사용
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";

    @Override
    public void saveRefreshToken(UUID userId, String refreshToken, Duration duration) {
        redisTemplate.opsForValue().set(RT_PREFIX + userId, refreshToken, duration);
    }

    @Override
    public Optional<String> findRefreshToken(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(RT_PREFIX + userId));
    }

    @Override
    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(RT_PREFIX + userId);
    }

    @Override
    public boolean existsByRefreshToken(UUID userId, String refreshToken) {
        // Redis에서 값(value)만으로 키를 찾는 것은 성능상 좋지 않으므로
        // 우선 false를 반환하거나 필요 시 로직을 추가 예정.
        String foundRefreshToken = redisTemplate.opsForValue().get(RT_PREFIX + userId);
        return refreshToken.equals(foundRefreshToken);
    }

    @Override
    public void saveBlacklist(UUID userId, String accessToken, Duration duration) {
        redisTemplate.opsForValue().set(BL_PREFIX + userId, accessToken, duration);
    }

    @Override
    public boolean existsByBlacklist(UUID userId, String accessToken) {
        String blacklistedToken = redisTemplate.opsForValue().get(BL_PREFIX + userId);
        return accessToken.equals(blacklistedToken);
    }
}
