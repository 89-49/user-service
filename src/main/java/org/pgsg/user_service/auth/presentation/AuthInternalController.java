package org.pgsg.user_service.auth.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.auth.presentation.dto.request.UserVerifyRequest;
import org.pgsg.user_service.auth.presentation.dto.response.UserVerifyResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/auth")
@RequiredArgsConstructor
public class AuthInternalController {

	private final TokenService tokenService;

	@PostMapping("/verify")
	public UserVerifyResponse verifyToken(@Valid @RequestBody UserVerifyRequest userVerifyRequest) {
		boolean blacklisted = tokenService.isBlacklisted(userVerifyRequest.accessToken());

		// isVerifiedToken = !blacklisted
		return new UserVerifyResponse(!blacklisted);
	}
}
