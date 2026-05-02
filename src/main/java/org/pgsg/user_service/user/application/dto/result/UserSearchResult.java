package org.pgsg.user_service.user.application.dto.result;

import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSearchResult(
		UUID userId,
		String username,
		String name,
		String nickname,
		UserRole userRole,
		LocalDateTime createdAt
) {
	public static UserSearchResult from(User user) {
		return new UserSearchResult(
				user.getUserId(),
				user.getUsername(),
				user.getName(),
				user.getNickname(),
				user.getUserRole(),
				user.getCreatedAt()
		);
	}
}
