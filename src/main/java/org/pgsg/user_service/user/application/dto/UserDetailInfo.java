package org.pgsg.user_service.user.application.dto;

import org.pgsg.user_service.user.domain.entity.ChatTimeRange;
import org.pgsg.user_service.user.domain.entity.User;
import org.pgsg.user_service.user.domain.entity.UserRole;

import java.util.List;
import java.util.UUID;

public record UserDetailInfo(
		UUID userId,
		String username,
		String password,
		UserRole userRole,
		String name,
		String nickname,
		boolean isEnabled,
		List<ChatTimeRangeInfo> chatTimeRange
		// TODO: 공통모듈 배포 이후 응답 데이터에 BaseEntity의 Auditing 필드 추가
) {
	public static List<ChatTimeRangeInfo> getChatTimeRangeInfos(List<ChatTimeRange> chatTimeRanges) {
		return chatTimeRanges.stream()
				.map(ChatTimeRangeInfo::from)
				.toList();
	}

	// User 엔티티 -> UserDetailInfo DTO
	public static UserDetailInfo from(User user) {
		return new UserDetailInfo(
				user.getUserId(),
				user.getUsername(),
				user.getPassword(),
				user.getUserRole(),
				user.getName(),
				user.getNickname(),
				user.isEnabled(),
				getChatTimeRangeInfos(user.getChatTimeRange())
		);
	}
}
