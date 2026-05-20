package org.pgsg.user_service.auth.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.user.application.UserQueryFacade;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticatorImplTest {

    @InjectMocks
    private UserAuthenticatorImpl userAuthenticator;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserQueryFacade userQueryFacade;

    private final UUID userId = UUID.randomUUID();
    private final String username = "testuser";
    private final String password = "password";
    private final String accessToken = "access.token";
    private final String refreshToken = "refresh.token";

    @Test
    @DisplayName("verify(username, password) - 성공: 인증 매니저를 통해 인증을 수행하고 UserDetails를 반환한다")
    void verify_Success() {
        // given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        // when
        UserDetailsImpl result = userAuthenticator.verify(username, password);

        // then
        assertThat(result).isEqualTo(userDetails);
        then(authenticationManager).should().authenticate(argThat(token -> 
            token.getPrincipal().equals(username) && token.getCredentials().equals(password)
        ));
    }

    @Test
    @DisplayName("verifyToken - 성공: 블랙리스트 확인 및 토큰 검증 후 최신 사용자 정보를 반환한다")
    void verifyToken_Success() {
        // given
        given(tokenService.isBlacklisted(accessToken)).willReturn(false);
        given(tokenService.verifyTokenPair(accessToken, refreshToken)).willReturn(userId);
        
        LoginUserDetailInfo loginInfo = new LoginUserDetailInfo(
                userId, username, "hashedPassword", UserRole.USER, "Name", "Nick", true
        );
        given(userQueryFacade.getUserForAuth(userId)).willReturn(loginInfo);

        // when
        UserDetailsImpl result = userAuthenticator.verifyToken(accessToken, refreshToken);

        // then
        assertThat(result.getUuid()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo(username);
        then(tokenService).should().isBlacklisted(accessToken);
        then(tokenService).should().verifyTokenPair(accessToken, refreshToken);
        then(userQueryFacade).should().getUserForAuth(userId);
    }

    @Test
    @DisplayName("verifyToken - 실패: 블랙리스트 토큰인 경우 예외를 발생시킨다")
    void verifyToken_Blacklisted() {
        // given
        given(tokenService.isBlacklisted(accessToken)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthenticator.verifyToken(accessToken, refreshToken))
                .isInstanceOf(UserServiceException.class);
        
        then(tokenService).should().isBlacklisted(accessToken);
        then(tokenService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("checkBlacklist - 실패: 블랙리스트 토큰인 경우 예외를 발생시킨다")
    void checkBlacklist_ThrowsException() {
        // given
        given(tokenService.isBlacklisted(accessToken)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthenticator.checkBlacklist(accessToken))
                .isInstanceOf(UserServiceException.class);
    }
}
