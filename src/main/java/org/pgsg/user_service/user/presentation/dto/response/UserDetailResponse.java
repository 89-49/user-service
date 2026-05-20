package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.info.ChatTimeRangeInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDetailResponse(
		UUID userId,
		String username,
		String name,
		String nickname,
		UserRole userRole,
		List<ChatTimeRangeInfo> chatTimeRanges,
		LocalDateTime createdAt,
		UUID createdBy,
		LocalDateTime modifiedAt,
		UUID modifiedBy,
		LocalDateTime deletedAt,
		UUID deletedBy
) {
	public static UserDetailResponse from(UserDetailInfo userDetailInfo) {
		return new UserDetailResponse(
				userDetailInfo.userId(),
				userDetailInfo.username(),
				userDetailInfo.name(),
				userDetailInfo.nickname(),
				userDetailInfo.userRole(),
				userDetailInfo.chatTimeRanges(),

				userDetailInfo.createdAt(),
				userDetailInfo.createdBy(),
				userDetailInfo.modifiedAt(),
				userDetailInfo.modifiedBy(),
				userDetailInfo.deletedAt(),
				userDetailInfo.deletedBy()
		);
	}
}
