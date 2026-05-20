package org.pgsg.user_service.auth.application.dto.info;

import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;

import java.time.LocalDateTime;
import java.util.UUID;

// Auth 도메인 내에서 가입 완료 정보를 전달하기 위한 전용 Info DTO
public record SignupInfo(
		UUID userId,
		String username,
		String nickname,
		LocalDateTime createdAt,
		UUID createdBy
) {
	public static SignupInfo from(UserDetailInfo userInfo) {
		return new SignupInfo(
				userInfo.userId(),
				userInfo.username(),
				userInfo.nickname(),
				userInfo.createdAt(),
				userInfo.createdBy()
		);
	}
}
