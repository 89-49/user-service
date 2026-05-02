package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.result.UserUpdateResult;

public record UserUpdateResponse(

) {
	public static UserUpdateResponse from(UserUpdateResult userUpdateResult) {
		return new UserUpdateResponse(

		);
	}
}
