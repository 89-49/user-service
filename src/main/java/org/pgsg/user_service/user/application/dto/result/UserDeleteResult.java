package org.pgsg.user_service.user.application.dto.result;

import org.pgsg.user_service.user.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDeleteResult(
		UUID userId,
		LocalDateTime deletedAt,
		UUID deletedBy
) {
	public static UserDeleteResult from(User user) {
		return new UserDeleteResult(
				user.getUserId(),
				user.getDeletedAt(),
				user.getDeletedBy()
		);
	}
}
