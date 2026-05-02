package org.pgsg.user_service.user.presentation.dto.request;

import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.model.UserRole;

public record UserSearchRequest(
		String keyword,
		String userRole,
		String nickname,
		String name,
		String username
) {
	public SearchUserQuery toQuery() {
		return new SearchUserQuery(
				keyword(),
				UserRole.find(userRole()),
				name(),
				nickname(),
				username()
		);
	}
}
