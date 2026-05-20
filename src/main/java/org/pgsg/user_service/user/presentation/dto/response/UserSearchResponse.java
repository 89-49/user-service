package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSearchResponse(
		UUID userId,
		String username,
		String name,
		String nickname,
		UserRole userRole,
		LocalDateTime createdAt
) {
	public static UserSearchResponse from(UserSearchResult searchResult) {
		return new UserSearchResponse(
				searchResult.userId(),
				searchResult.username(),
				searchResult.name(),
				searchResult.nickname(),
				searchResult.userRole(),
				searchResult.createdAt()
		);
	}
}
