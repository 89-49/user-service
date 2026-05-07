package org.pgsg.user_service.auth.presentation.dto.response;

import org.pgsg.user_service.auth.application.dto.info.AuthInfo;

public record UserLoginResponse(
    String accessToken,
    String refreshToken
) {
    public static UserLoginResponse from(AuthInfo info) {
        return new UserLoginResponse(info.accessToken(), info.refreshToken());
    }
}