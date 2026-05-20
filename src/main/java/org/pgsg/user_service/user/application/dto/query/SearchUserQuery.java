package org.pgsg.user_service.user.application.dto.query;

import org.pgsg.user_service.user.domain.model.UserRole;

public record SearchUserQuery(
		String keyword,
		UserRole userRole,
		String nickname,
		String name,
		String username
) {
}
