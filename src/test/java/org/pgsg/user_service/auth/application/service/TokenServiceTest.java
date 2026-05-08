package org.pgsg.user_service.auth.application.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.config.security.jwt.JwtProperties;
import org.pgsg.config.security.token.TokenPair;
import org.pgsg.config.security.token.TokenProvider;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    private final UUID userId = UUID.randomUUID();
    private final String accessToken = "mock.access.token";
    private final String refreshToken = "mock.refresh.token";

    @Test
    @DisplayName("issueTokenPair - 성공: 토큰 쌍을 생성하고 리프레시 토큰을 저장한다")
    void issueTokenPair_Success() {
        // given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        given(userDetails.getUuid()).willReturn(userId);
        
        TokenPair tokenPair = mock(TokenPair.class);
        given(tokenPair.getAccessToken()).willReturn(accessToken);
        given(tokenPair.getRefreshToken()).willReturn(refreshToken);
        
        given(tokenProvider.createTokenPair(userDetails)).willReturn(tokenPair);
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(3600000L);

        // when
        AuthInfo result = tokenService.issueTokenPair(userDetails);

        // then
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
        then(tokenRepository).should().saveRefreshToken(eq(userId), eq(refreshToken), eq(Duration.ofMillis(3600000L)));
    }

    @Test
    @DisplayName("verifyTokenPair - 성공: 유효한 토큰 쌍인 경우 사용자 ID를 반환한다")
    void verifyTokenPair_Success() {
        // given
        given(tokenProvider.getSubjectFromExpiredAccessToken(accessToken)).willReturn(userId.toString());
        given(tokenProvider.validateToken(refreshToken)).willReturn(true);
        
        Claims claims = mock(Claims.class);
        given(claims.get(anyString(), eq(String.class))).willReturn("refresh");
        given(tokenProvider.parseClaims(refreshToken)).willReturn(claims);
        given(tokenProvider.getUserId(refreshToken)).willReturn(userId);
        
        given(tokenRepository.findRefreshToken(userId)).willReturn(Optional.of(refreshToken));

        // when
        UUID result = tokenService.verifyTokenPair(accessToken, refreshToken);

        // then
        assertThat(result).isEqualTo(userId);
    }

    @Test
    @DisplayName("verifyTokenPair - 실패: 토큰 정보가 일치하지 않거나 유효하지 않으면 예외가 발생한다")
    void verifyTokenPair_Fail() {
        // given
        given(tokenProvider.getSubjectFromExpiredAccessToken(accessToken)).willReturn(userId.toString());
        given(tokenProvider.validateToken(refreshToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> tokenService.verifyTokenPair(accessToken, refreshToken))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("validateRefreshToken - 성공: 저장된 리프레시 토큰과 일치하면 통과한다")
    void validateRefreshToken_Success() {
        // given
        given(tokenRepository.findRefreshToken(userId)).willReturn(Optional.of(refreshToken));

        // when & then
        tokenService.validateRefreshToken(userId, refreshToken);
        then(tokenRepository).should().findRefreshToken(userId);
    }

    @Test
    @DisplayName("validateRefreshToken - 실패: 저장된 리프레시 토큰이 없거나 일치하지 않으면 예외가 발생한다")
    void validateRefreshToken_Fail() {
        // given
        given(tokenRepository.findRefreshToken(userId)).willReturn(Optional.of("other.refresh.token"));

        // when & then
        assertThatThrownBy(() -> tokenService.validateRefreshToken(userId, refreshToken))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("deleteRefreshToken - 성공: 리프레시 토큰을 삭제한다")
    void deleteRefreshToken_Success() {
        // when
        tokenService.deleteRefreshToken(userId);

        // then
        then(tokenRepository).should().deleteRefreshToken(userId);
    }
}
