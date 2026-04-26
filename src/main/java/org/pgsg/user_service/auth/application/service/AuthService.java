package org.pgsg.user_service.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.domain.JwtTokenProvider;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.auth.domain.model.TokenPair;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;  // 분리할 경우 내부적으로 FeignClient를 사용하는 UserProvider로 대체 필요
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final UserAuthenticator userAuthenticator;
    private final PasswordEncoder passwordEncoder;

    //로그인 기능
    @Transactional
    public AuthInfo login(LoginUserCommand command) {

        // 최초 로그인 시 비밀번호 검증
        UserDetailsImpl userDetail = userAuthenticator.verify(command.getUsername(), command.getPassword());

        // 토큰 생성 및 저장
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(userDetail);
        tokenRepository.saveRefreshToken(userDetail.getUuid(), tokenPair.getRefreshToken(), Duration.ofDays(7));

        return AuthInfo.from(tokenPair);
    }

    //로그아웃 기능
    @Transactional
    public void logout(UUID userId) {
        tokenRepository.deleteRefreshToken(userId);
    }

    @Transactional
    public SignupInfo signup(SignupUserCommand command) {
        String encryptedPassword = passwordEncoder.encode(command.password());
        CreateUserCommand createUserCommand = new CreateUserCommand(
                command.username(),
                encryptedPassword,
                command.userRole(),
                command.name(),
                command.nickname(),
                command.chatTimeRanges()
        );

        UserDetailInfo userInfo = userService.createUser(createUserCommand);
        return SignupInfo.from(userInfo);
    }
}