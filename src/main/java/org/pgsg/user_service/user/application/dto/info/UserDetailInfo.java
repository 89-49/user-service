package org.pgsg.user_service.user.application.dto.info;

import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserDetailInfo(
		UUID userId,
		String username,
		UserRole userRole,
		String name,
		String nickname,
		List<ChatTimeRangeInfo> chatTimeRange
		// TODO: 공통모듈 배포 이후 응답 데이터에 BaseEntity의 Auditing 필드 추가
) {
	public static List<ChatTimeRangeInfo> getChatTimeRangeInfos(List<ChatTimeRange> chatTimeRanges) {
		if (chatTimeRanges == null || chatTimeRanges.isEmpty()) {
			return List.of();
		}
		return chatTimeRanges.stream()
				.filter(Objects::nonNull)
				.map(ChatTimeRangeInfo::from)
				.toList();
	}

	// User 엔티티 -> UserDetailInfo DTO
	public static UserDetailInfo from(User user) {
		return new UserDetailInfo(
				user.getUserId(),
				user.getUsername(),
				user.getUserRole(),
				user.getName(),
				user.getNickname(),
				getChatTimeRangeInfos(user.getChatTimeRange())
		);
	}
}
