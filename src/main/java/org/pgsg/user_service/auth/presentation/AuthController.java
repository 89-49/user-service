package org.pgsg.user_service.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.service.AuthService;
import org.pgsg.user_service.auth.presentation.dto.request.UserLoginRequest;
import org.pgsg.user_service.auth.presentation.dto.response.UserLoginResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserLoginResponse login(
            @RequestBody UserLoginRequest request) {

        AuthInfo info = authService.login(request.toCommand());

        return UserLoginResponse.from(info);
    }
}