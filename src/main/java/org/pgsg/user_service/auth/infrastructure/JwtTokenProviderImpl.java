package org.pgsg.user_service.auth.infrastructure;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.domain.JwtTokenProvider;
import org.pgsg.user_service.auth.domain.vo.TokenPair;
import org.pgsg.user_service.user.domain.entity.UserRole;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    /**
     * 의존성 주입 완료 후, 설정 파일(yml)에서 가져온 SecretKey를
     * JWT 서명에 사용할 수 있는 Key 객체로 변환하여 초기화합니다.
     */
    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 사용자의 식별값(UUID)과 권한을 받아 Access Token과 Refresh Token 쌍을 생성합니다.
     * 생성된 토큰들은 TokenPair VO 객체에 담겨 반환됩니다.
     */
    @Override
    public TokenPair createTokenPair(UUID userId, UserRole role) {
        String accessToken = createToken(userId, role, jwtProperties.getAccessTokenExpiration());
        String refreshToken = createToken(userId, role, jwtProperties.getRefreshTokenExpiration());
        return TokenPair.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * 토큰 내부의 Payload에서 사용자의 식별값(Subject)을 추출하여 UUID 형태로 반환합니다.
     */
    @Override
    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    /**
     * 토큰의 유효성을 검사합니다.
     * 서명 위조, 만료 여부, 형식 오류 등을 체크하며 문제가 없으면 true를 반환합니다.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 만료된 Access Token으로부터 사용자의 식별값(Subject)을 추출합니다.
     * 토큰 재발급(Reissue) 시, 만료된 토큰의 정보를 확인하기 위해 사용됩니다.
     */
    @Override
    public String getSubjectFromExpiredAccessToken(String accessToken) {
        try {
            // 만료되지 않은 경우 정상적으로 Subject 추출
            return parseClaims(accessToken).getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 경우 예외 객체에 담긴 Claims에서 Subject 추출
            return e.getClaims().getSubject();
        }
    }

    /**
     * 실제 JWT를 생성하는 공통 내부 메서드입니다.
     * 사용자 ID를 Subject에 넣고, 권한(role)을 Custom Claim으로 추가하여 서명합니다.
     */
    private String createToken(UUID userId, UserRole role, Long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.getRole())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰을 복호화(파싱)하여 내부의 데이터 묶음인 Claims를 추출합니다.
     * 서명 검증을 포함하며, 라이브러리 버전에 따라 parseSignedClaims와 getPayload를 사용합니다.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}