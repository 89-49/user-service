package org.pgsg.user_service.auth.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.application.service.AuthService;
import org.pgsg.user_service.auth.presentation.dto.request.UserLoginRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserReissueRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserSignupRequest;
import org.pgsg.user_service.auth.presentation.dto.response.UserLoginResponse;
import org.pgsg.user_service.auth.presentation.dto.response.UserSignupResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserLoginResponse login(@Valid @RequestBody UserLoginRequest request) {

        AuthInfo info = authService.login(request.toCommand());

        return UserLoginResponse.from(info);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void logout(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken
    ) {
        // 헤더에서 받은 UUID와 토큰을 서비스에 전달
        authService.logout(userId, accessToken);

        return null;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSignupResponse signup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        SignupInfo info = authService.signup(userSignupRequest.toCommand());

        return UserSignupResponse.from(info);
    }

    @PostMapping("/reissue")
    public UserLoginResponse reissue(@Valid @RequestBody UserReissueRequest userReissueRequest) {
        AuthInfo info = authService.reissue(userReissueRequest.toCommand());

        return UserLoginResponse.from(info);
    }
}
