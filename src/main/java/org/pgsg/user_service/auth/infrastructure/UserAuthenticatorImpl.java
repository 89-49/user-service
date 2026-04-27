package org.pgsg.user_service.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthenticatorImpl implements UserAuthenticator {

    // 공통 모듈에서 커스터마이징한 UserDetailsImpl을 사용하기 위해 AuthenticationManager를 사용
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetailsImpl verify(String username, String rawPassword) {
        // authenticationManager의 내부 로직에서 비밀번호 검증도 수행(등록된 BCryptPasswordEncoder 사용)
        // 공통모듈에서 커스텀한 UserDetailsImpl을 사용하는 경우에는 AuthenticationManager를 통한 인증 수행
        Authentication authenticated = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );

        return (UserDetailsImpl) authenticated.getPrincipal();
    }

    // 추가적인 검증 로직 (예: 계정 잠금, 휴면 계정 체크 등)을 여기에 확장할 수 있습니다.
}