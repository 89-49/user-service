package org.pgsg.user_service.auth.application.dto.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.user_service.auth.domain.vo.TokenPair;

@Getter
@AllArgsConstructor
public class AuthInfo {
    private String accessToken;
    private String refreshToken;

    public static AuthInfo from(TokenPair tokenPair) {
        return new AuthInfo(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
    }
}