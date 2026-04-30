package org.pgsg.user_service.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

// TODO: 로그아웃 여부 등 실시간 상태 확인은 반드시 여기서 checkBlacklist를 통해 수행 - user-service에 유지

@Component
@RequiredArgsConstructor
public class UserAuthenticatorImpl implements UserAuthenticator {

    // 공통 모듈에서 커스터마이징한 UserDetailsImpl을 사용하기 위해 AuthenticationManager를 사용
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;

    @Override
    public UserDetailsImpl verify(String username, String rawPassword) {
        // authenticationManager의 내부 로직에서 비밀번호 검증도 수행(등록된 BCryptPasswordEncoder 사용)
        // 공통모듈에서 커스텀한 UserDetailsImpl을 사용하는 경우에는 AuthenticationManager를 통한 인증 수행
        Authentication authenticated = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );

        return (UserDetailsImpl) authenticated.getPrincipal();
    }

    @Override
    public UserDetailsImpl verifyToken(String accessToken, String refreshToken) {
        // 1. 블랙리스트 확인
        checkBlacklist(accessToken);

        // 2. 토큰 서비스에게 토큰 쌍 검증 및 사용자 ID 추출 위임
        UUID userId = tokenService.verifyTokenPair(accessToken, refreshToken);

        // 3. 최신 사용자 정보를 담은 UserDetailsImpl 반환 (인증기 본연의 역할)
        return userService.getUserForAuth(userId).toUserDetails();
    }

    @Override
    public void checkBlacklist(String accessToken) {
        if (tokenService.isBlacklisted(accessToken)) {
            throw new UserServiceException(UserErrorCode.UNAUTHORIZED);
        }
    }

    // 추가적인 검증 로직 (예: 계정 잠금, 휴면 계정 체크 등)을 여기에 확장할 수 있습니다.
}
