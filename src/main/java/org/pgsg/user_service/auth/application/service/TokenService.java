package org.pgsg.user_service.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.domain.TokenProvider;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.pgsg.user_service.auth.domain.model.TokenPair;
import org.pgsg.user_service.auth.infrastructure.security.jwt.JwtProperties;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

// TODO: 토큰 비즈니스 로직을 통합적으로 관리 -> user-service에 유지
@Service
@RequiredArgsConstructor
public class TokenService {

	private final TokenProvider tokenProvider;
	private final TokenRepository tokenRepository;
	private final JwtProperties jwtProperties;

	/**
	 * 사용자의 인증 정보를 바탕으로 토큰 쌍(Access/Refresh)을 발급하고,
	 * Refresh Token을 저장소(Redis)에 기록합니다.
	 */
	public AuthInfo issueTokenPair(UserDetailsImpl userDetails) {
		TokenPair tokenPair = tokenProvider.createTokenPair(userDetails);

		tokenRepository.saveRefreshToken(
				userDetails.getUuid(),
				tokenPair.getRefreshToken(),
				Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
		);

		return AuthInfo.from(tokenPair);
	}

	/**
	 * Access Token(만료)과 Refresh Token(유효) 쌍을 검증하고 사용자 ID를 반환합니다.
	 */
	public UUID verifyTokenPair(String accessToken, String refreshToken) {
		// 1. 만료된 Access Token에서 사용자 ID 추출
		UUID userIdFromAccess = UUID.fromString(tokenProvider.getSubjectFromExpiredAccessToken(accessToken));

		// 2. Refresh Token 유효성 검증 및 교차 검증
		UUID userIdFromRefresh = Optional.of(refreshToken)
				.filter(tokenProvider::validateToken)
				.map(tokenProvider::getUserId)
				.filter(userIdFromAccess::equals) // 교차 검증
				.orElseThrow(() -> new UserServiceException("UnauthorizedException"));

		// 3. 저장소(Redis) 일치 여부 확인
		validateRefreshToken(userIdFromRefresh, refreshToken);

		return userIdFromRefresh;
	}


	// refreshToken 관리 기능

	public void validateRefreshToken(UUID userId, String refreshToken) {
		tokenRepository.findRefreshTokenHash(userId)
				.filter(refreshToken::equals)
				.orElseThrow(() -> new UserServiceException("UnauthorizedException"));
	}

	public void deleteRefreshToken(UUID userId) {
		tokenRepository.deleteRefreshToken(userId);
	}


	// 토큰 블랙리스트 검증 관련 기능

	public void addToBlacklist(String accessToken) {
		long remainingTime = tokenProvider.getRemainingTime(accessToken);
		if (remainingTime > 0) {
			tokenRepository.saveBlacklist(accessToken, Duration.ofMillis(remainingTime));
		}
	}

	public boolean isBlacklisted(String accessToken) {
		return tokenRepository.existsByBlacklist(accessToken);
	}
}
