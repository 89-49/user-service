package org.pgsg.user_service.user.presentation.dto;

import org.pgsg.user_service.user.application.dto.UserDetailsInfo;
import org.pgsg.user_service.user.domain.entity.UserRole;

import java.util.UUID;

// UserDetailsImpl에 저장할 회원정보를 응답으로 반환하기 위한 DTO
public record GetUserDetailsResponse(
		UUID userId,
		String username,
		String password,	// 이미 BCrypt에 의해 암호화된 비밀번호를 반환
		UserRole userRole,
		String name,
		String nickname,
		boolean isEnabled
) {
	// application 계층 UserDetailsInfo -> presentation 계층 GetDetailsResponse 로 변환
	public static GetUserDetailsResponse from(UserDetailsInfo userDetailsInfo) {
		return new GetUserDetailsResponse(
				userDetailsInfo.userId(),
				userDetailsInfo.username(),
				userDetailsInfo.password(),
				userDetailsInfo.userRole(),
				userDetailsInfo.name(),
				userDetailsInfo.nickname(),
				userDetailsInfo.isEnabled()
		);
	}
}
