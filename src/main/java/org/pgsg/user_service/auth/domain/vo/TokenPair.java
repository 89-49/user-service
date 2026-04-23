package org.pgsg.user_service.auth.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenPair {
    private final String accessToken;
    private final String refreshToken;
    private final Long accessTokenExpiresIn;

    public static TokenPair of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenPair(accessToken, refreshToken, expiresIn);
    }
}