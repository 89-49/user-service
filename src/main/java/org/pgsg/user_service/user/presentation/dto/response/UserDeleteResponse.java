package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.result.UserDeleteResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDeleteResponse(
		UUID userId,
		LocalDateTime deletedAt,
		UUID deletedBy
) {
	public static UserDeleteResponse from(UserDeleteResult deletedResult) {
		return new UserDeleteResponse(
				deletedResult.userId(),
				deletedResult.deletedAt(),
				deletedResult.deletedBy()
		);
	}
}
