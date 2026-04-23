package org.pgsg.user_service.user.presentation.dto;

import org.pgsg.user_service.user.application.dto.UserDetailInfo;
import org.pgsg.user_service.user.domain.entity.UserRole;

import java.util.UUID;

// UserDetailsImpl에 저장할 회원정보를 응답으로 반환하기 위한 DTO
public record LoginUserResponse(
		UUID userId,
		String username,
		String password,	// 이미 BCrypt에 의해 암호화된 비밀번호를 반환
		UserRole userRole,
		String name,
		String nickname,
		boolean isEnabled
) {
	// application 계층 UserDetailsInfo -> presentation 계층 GetDetailsResponse 로 변환
	public static LoginUserResponse from(UserDetailInfo userDetailInfo) {
		return new LoginUserResponse(
				userDetailInfo.userId(),
				userDetailInfo.username(),
				userDetailInfo.password(),
				userDetailInfo.userRole(),
				userDetailInfo.name(),
				userDetailInfo.nickname(),
				userDetailInfo.isEnabled()
		);
	}
}
