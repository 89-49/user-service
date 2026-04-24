package org.pgsg.user_service.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;

    public static UserLoginResponse from(AuthInfo info) {
        return new UserLoginResponse("Bearer " + info.getAccessToken(), info.getRefreshToken());
    }
}