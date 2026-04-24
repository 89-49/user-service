package org.pgsg.user_service.auth.domain;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.LoginUserDetailInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthenticator {
    private final PasswordEncoder passwordEncoder;

    public void verify(LoginUserDetailInfo user, String rawPassword) {
        // user.getPassword()가 암호화된 비밀번호를 반환한다고 가정합니다.
        if (!passwordEncoder.matches(rawPassword, user.password())) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다. 로그인에 실패했습니다.");
        }

        // 추가적인 검증 로직 (예: 계정 잠금, 휴면 계정 체크 등)을 여기에 확장할 수 있습니다.
    }
}