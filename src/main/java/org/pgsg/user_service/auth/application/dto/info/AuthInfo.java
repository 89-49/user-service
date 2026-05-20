package org.pgsg.user_service.auth.application.dto.info;

import org.pgsg.config.security.token.TokenPair;

public record AuthInfo(
    String accessToken,
    String refreshToken
) {
    public static AuthInfo from(TokenPair tokenPair) {
        return new AuthInfo(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
    }
}