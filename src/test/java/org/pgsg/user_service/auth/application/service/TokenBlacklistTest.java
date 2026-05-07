package org.pgsg.user_service.auth.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.token.TokenProvider;
import org.pgsg.user_service.auth.domain.TokenRepository;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Token Blacklist 관련 테스트")
class TokenBlacklistTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenRepository tokenRepository;

    private final UUID userId = UUID.randomUUID();
    private final String accessToken = "mock.access.token";

    @Test
    @DisplayName("addToBlacklist - 성공: 토큰의 남은 유효시간만큼 블랙리스트에 저장한다")
    void addToBlacklist_Success() {
        // given
        long remainingTime = 10000L;
        given(tokenProvider.getRemainingTime(anyString())).willReturn(remainingTime);

        // when
        tokenService.addToBlacklist(userId, "Bearer " + accessToken);

        // then
        then(tokenRepository).should().saveBlacklist(eq(userId), eq(accessToken), eq(Duration.ofMillis(remainingTime)));
    }

    @Test
    @DisplayName("addToBlacklist - 실패: 토큰 파싱 실패 시 에러 로그를 남기고 정상 종료된다 (예외 전파 안됨)")
    void addToBlacklist_ParsingFail() {
        // when
        tokenService.addToBlacklist(userId, null);

        // then
        then(tokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("isBlacklisted - 성공: 블랙리스트에 존재하면 true를 반환한다")
    void isBlacklisted_True() {
        // given
        given(tokenProvider.getSubjectFromExpiredAccessToken(anyString())).willReturn(userId.toString());
        given(tokenRepository.existsByBlacklist(userId, accessToken)).willReturn(true);

        // when
        boolean result = tokenService.isBlacklisted("Bearer " + accessToken);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isBlacklisted - 성공: 블랙리스트에 없으면 false를 반환한다")
    void isBlacklisted_False() {
        // given
        given(tokenProvider.getSubjectFromExpiredAccessToken(anyString())).willReturn(userId.toString());
        given(tokenRepository.existsByBlacklist(userId, accessToken)).willReturn(false);

        // when
        boolean result = tokenService.isBlacklisted("Bearer " + accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isBlacklisted - 예외 발생: 블랙리스트인 것으로 간주(true 반환)한다")
    void isBlacklisted_Exception_ReturnsTrue() {
        // given
        given(tokenProvider.getSubjectFromExpiredAccessToken(anyString())).willThrow(new RuntimeException("Parsing Error"));

        // when
        boolean result = tokenService.isBlacklisted("Bearer " + accessToken);

        // then
        assertThat(result).isTrue();
    }
}
