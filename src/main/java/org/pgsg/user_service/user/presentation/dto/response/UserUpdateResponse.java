package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.result.UserUpdateResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserUpdateResponse(
		UUID userId,
		LocalDateTime modifiedAt,
		UUID modifiedBy
) {
	public static UserUpdateResponse from(UserUpdateResult userUpdateResult) {
		return new UserUpdateResponse(
				userUpdateResult.userId(),
				userUpdateResult.modifiedAt(),
				userUpdateResult.modifiedBy()
		);
	}
}
