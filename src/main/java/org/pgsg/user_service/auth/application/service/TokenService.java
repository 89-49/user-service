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
	 * 저장소(Redis)에 저장된 리프레시 토큰과 전달받은 토큰이 일치하는지 검증합니다.
	 */
	public void validateRefreshToken(UUID userId, String refreshToken) {
		tokenRepository.findRefreshTokenHash(userId)
				.filter(refreshToken::equals)
				.orElseThrow(() -> new UserServiceException("UnauthorizedException"));
	}

	/**
	 * 저장소(Redis)에서 해당 사용자의 리프레시 토큰을 삭제합니다.
	 */
	public void deleteRefreshToken(UUID userId) {
		tokenRepository.deleteRefreshToken(userId);
	}

	/**
	 * 액세스 토큰을 블랙리스트에 등록합니다.
	 * 토큰의 남은 유효 시간만큼만 저장소에 유지합니다.
	 */
	public void addToBlacklist(UUID userId, String accessToken) {
		long remainingTime = tokenProvider.getRemainingTime(accessToken);
		if (remainingTime > 0) {
			tokenRepository.saveBlacklist(userId, accessToken, Duration.ofMillis(remainingTime));
		}
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

	/**
	 * 블랙리스트 여부 확인
	 */
	public boolean isBlacklisted(String accessToken) {
		// 토큰에서 사용자 ID를 추출하여 해당 키로 저장된 토큰이 있는지 확인
		UUID userId = UUID.fromString(tokenProvider.getSubjectFromExpiredAccessToken(accessToken));
		return tokenRepository.existsByBlacklist(userId, accessToken);
	}
}
