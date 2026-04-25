package org.pgsg.user_service.auth.presentation.dto.response;

import org.pgsg.user_service.auth.application.dto.info.SignupInfo;

import java.util.UUID;

public record UserSignupResponse(
		UUID userId,
		String username,
		String nickname
		// TODO: 공통모듈 도입 이후 createdAt, createdBy 필드 추가
) {
	public static UserSignupResponse from(SignupInfo signupInfo) {
		return new UserSignupResponse(
				signupInfo.userId(),
				signupInfo.username(),
				signupInfo.nickname()
				// TODO: 공통모듈 도입 이후 createdAt, createdBy 필드 추가
		);
	}
}
