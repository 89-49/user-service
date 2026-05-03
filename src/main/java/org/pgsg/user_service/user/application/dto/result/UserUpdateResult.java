package org.pgsg.user_service.user.application.dto.result;

import org.pgsg.user_service.user.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserUpdateResult(
		UUID userId,
		LocalDateTime modifiedAt,
		UUID modifiedBy
) {
	public static UserUpdateResult from(User user) {
		return new UserUpdateResult(
				user.getUserId(),
				user.getModifiedAt(),
				user.getModifiedBy()
		);
	}
}
