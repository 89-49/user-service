package org.pgsg.user_service.auth.application.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.config.security.jwt.JwtProperties;
import org.pgsg.config.security.jwt.JwtUtils;
import org.pgsg.config.security.token.TokenPair;
import org.pgsg.config.security.token.TokenProvider;
import org.pgsg.config.security.token.TokenType;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;

import org.pgsg.user_service.auth.domain.TokenRepository;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
		tokenRepository.findRefreshToken(userId)
				.filter(refreshToken::equals)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.UNAUTHORIZED));
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
		try {
			String normalizedToken = JwtUtils.normalizeToken(accessToken);
			long remainingTime = tokenProvider.getRemainingTime(normalizedToken);
			if (remainingTime > 0) {
				tokenRepository.saveBlacklist(userId, normalizedToken, Duration.ofMillis(remainingTime));
			}
		} catch (JwtException | IllegalArgumentException e) {
			log.error("[TokenService] 블랙리스트 등록 중 오류 발생 (토큰 파싱 실패 등): {}", e.getMessage());
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
				.filter(token -> TokenType.REFRESH.matches(tokenProvider.parseClaims(token).get(JwtUtils.CLAIM_TOKEN_TYPE, String.class))) // 토큰 타입 검증 추가
				.map(tokenProvider::getUserId)
				.filter(userIdFromAccess::equals) // 교차 검증
				.orElseThrow(() -> new UserServiceException(UserErrorCode.UNAUTHORIZED));

		// 3. 저장소(Redis) 일치 여부 확인
		validateRefreshToken(userIdFromRefresh, refreshToken);

		return userIdFromRefresh;
	}

	/**
	 * 블랙리스트 여부 확인
	 */
	public boolean isBlacklisted(String accessToken) {
		// 토큰에서 사용자 ID를 추출하여 해당 키로 저장된 토큰이 있는지 확인
		try {
			String normalizedToken = JwtUtils.normalizeToken(accessToken);
			UUID userId = UUID.fromString(tokenProvider.getSubjectFromExpiredAccessToken(normalizedToken));
			return tokenRepository.existsByBlacklist(userId, normalizedToken);
		} catch (Exception e) {
			// try 블록 안에서 발생한 모든 예외 상황은 토큰이 블랙리스트에 포함된 것과 동일하게 간주
			return true;
		}
	}
}
