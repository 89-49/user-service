package org.pgsg.user_service.auth.domain;

import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.domain.model.TokenPair;

import java.util.UUID;

public interface JwtTokenProvider {

    // 사용자 식별값과 권한 정보를 바탕으로 토큰 생성
    TokenPair createTokenPair(UserDetailsImpl userDetails);

    // 토큰에서 사용자 식별값(Subject) 추출
    UUID getUserId(String token);

    // 토큰 유효성 및 만료 여부 확인
    boolean validateToken(String token);

    // 만료된 토큰에서도 클레임을 추출 (재발급 로직용)
    String getSubjectFromExpiredAccessToken(String accessToken);
}