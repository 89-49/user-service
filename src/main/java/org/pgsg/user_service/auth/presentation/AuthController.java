package org.pgsg.user_service.auth.presentation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.application.service.AuthService;
import org.pgsg.user_service.auth.presentation.dto.request.UserLoginRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserReissueRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserSignupRequest;
import org.pgsg.user_service.auth.presentation.dto.response.UserLoginResponse;
import org.pgsg.user_service.auth.presentation.dto.response.UserSignupResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public void logout(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        // 인증 객체에서 UUID를 꺼내 서비스에 로그아웃 요청
        authService.logout(userDetails.getUuid(), accessToken);
    }

    @PostMapping("/signup")
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