package org.pgsg.user_service.auth.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;
import org.pgsg.user_service.auth.application.dto.command.ReissueUserCommand;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.user.application.UserCommandFacade;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserCommandFacade userCommandFacade;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UUID userId = UUID.randomUUID();
    private final String username = "testuser";
    private final String password = "password123";
    private final String accessToken = "access.token";

	@Test
    @DisplayName("login - 성공: 사용자 인증 후 토큰을 발급한다")
    void login_Success() {
        // given
        LoginUserCommand command = new LoginUserCommand(username, password);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        AuthInfo expectedAuthInfo = mock(AuthInfo.class);

        when(userAuthenticator.verify(username, password)).thenReturn(userDetails);
        when(tokenService.issueTokenPair(userDetails)).thenReturn(expectedAuthInfo);

        // when
        AuthInfo result = authService.login(command);

        // then
        assertThat(result).isEqualTo(expectedAuthInfo);
        verify(userAuthenticator).verify(username, password);
        verify(tokenService).issueTokenPair(userDetails);
    }

    @Test
    @DisplayName("logout - 성공: 리프레시 토큰을 삭제하고 액세스 토큰을 블랙리스트에 추가한다")
    void logout_Success() {
        // when
        authService.logout(userId, accessToken);

        // then
        verify(tokenService).deleteRefreshToken(userId);
        verify(tokenService).addToBlacklist(userId, accessToken);
    }

    @Test
    @DisplayName("signup - 성공: 비밀번호를 암호화하고 사용자를 생성한다")
    void signup_Success() {
        // given
        SignupUserCommand command = new SignupUserCommand(
                username, password, UserRole.USER, "Name", "Nickname", Collections.emptyList()
        );
        String encryptedPassword = "encryptedPassword";
        
        UserDetailInfo userDetailInfo = mock(UserDetailInfo.class);
        when(userDetailInfo.userId()).thenReturn(userId);
        when(userDetailInfo.username()).thenReturn(username);
        when(userDetailInfo.nickname()).thenReturn("Nickname");
        when(userDetailInfo.createdAt()).thenReturn(LocalDateTime.now());
        when(userDetailInfo.createdBy()).thenReturn(userId);

        when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(userCommandFacade.createUser(any())).thenReturn(userDetailInfo);

        // when
        SignupInfo result = authService.signup(command);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo(username);
        verify(passwordEncoder).encode(password);
        verify(userCommandFacade).createUser(argThat(createUserCommand -> 
            createUserCommand.password().equals(encryptedPassword) &&
            createUserCommand.username().equals(username)
        ));
    }

    @Test
    @DisplayName("reissue - 성공: 토큰 검증 후 새로운 토큰을 발급한다")
    void reissue_Success() {
        // given
		String refreshToken = "refresh.token";
		ReissueUserCommand command = new ReissueUserCommand(accessToken, refreshToken);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        AuthInfo expectedAuthInfo = mock(AuthInfo.class);

        when(userAuthenticator.verifyToken(accessToken, refreshToken)).thenReturn(userDetails);
        when(tokenService.issueTokenPair(userDetails)).thenReturn(expectedAuthInfo);

        // when
        AuthInfo result = authService.reissue(command);

        // then
        assertThat(result).isEqualTo(expectedAuthInfo);
        verify(userAuthenticator).verifyToken(accessToken, refreshToken);
        verify(tokenService).issueTokenPair(userDetails);
    }
}
