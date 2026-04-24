package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.info.ChatTimeRangeInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.List;
import java.util.UUID;

public record UserDetailResponse(
		UUID userId,
		String username,
		String name,
		String nickname,
		UserRole userRole,
		List<ChatTimeRangeInfo> chatTimeRange
		// TODO: 공통모듈 배포 이후 응답 데이터에 BaseEntity의 Auditing 필드 추가
) {
	public static UserDetailResponse from(UserDetailInfo userDetailInfo) {
		return new UserDetailResponse(
				userDetailInfo.userId(),
				userDetailInfo.username(),
				userDetailInfo.name(),
				userDetailInfo.nickname(),
				userDetailInfo.userRole(),
				userDetailInfo.chatTimeRange()
				// TODO: 공통모듈 배포 이후 응답 데이터에 BaseEntity의 Auditing 필드 추가
		);
	}
}
