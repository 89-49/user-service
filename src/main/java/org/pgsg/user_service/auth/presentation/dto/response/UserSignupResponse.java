package org.pgsg.user_service.auth.presentation.dto.response;

import org.pgsg.user_service.auth.application.dto.info.SignupInfo;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSignupResponse(
		UUID userId,
		String username,
		String nickname,
		LocalDateTime createdAt,
		UUID createdBy
) {
	public static UserSignupResponse from(SignupInfo signupInfo) {
		return new UserSignupResponse(
				signupInfo.userId(),
				signupInfo.username(),
				signupInfo.nickname(),
				signupInfo.createdAt(),
				signupInfo.createdBy()
		);
	}
}
