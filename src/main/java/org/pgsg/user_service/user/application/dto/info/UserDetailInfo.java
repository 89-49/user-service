package org.pgsg.user_service.user.application.dto.info;

import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserDetailInfo(
		UUID userId,
		String username,
		UserRole userRole,
		String name,
		String nickname,
		List<ChatTimeRangeInfo> chatTimeRanges,

		LocalDateTime createdAt,
		UUID createdBy,
		LocalDateTime modifiedAt,
		UUID modifiedBy,
		LocalDateTime deletedAt,
		UUID deletedBy
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
				getChatTimeRangeInfos(user.getChatTimeRanges()),

				user.getCreatedAt(),
				user.getCreatedBy(),
				user.getModifiedAt(),
				user.getModifiedBy(),
				user.getDeletedAt(),
				user.getDeletedBy()
		);
	}
}
