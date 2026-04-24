package org.pgsg.user_service.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.domain.JwtTokenProvider;
import org.pgsg.user_service.auth.domain.TokenRepository;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.auth.domain.model.TokenPair;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final UserAuthenticator userAuthenticator;

    public AuthInfo login(LoginUserCommand command) {

        LoginUserDetailInfo userDetail = userService.getUser(command.getUsername());

        // 비밀번호 검증
        userAuthenticator.verify(userDetail, command.getPassword());

        // 토큰 생성 및 저장
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(userDetail.userId(), userDetail.userRole());
        tokenRepository.saveRefreshToken(userDetail.userId(), tokenPair.getRefreshToken(), Duration.ofDays(7));

        return AuthInfo.from(tokenPair);
    }
}