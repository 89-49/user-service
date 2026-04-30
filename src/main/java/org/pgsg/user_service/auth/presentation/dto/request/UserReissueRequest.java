package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.pgsg.user_service.auth.application.dto.command.ReissueUserCommand;

public record UserReissueRequest(

		@NotBlank
		String accessToken,

		@NotBlank
		String refreshToken
) {
	public ReissueUserCommand toCommand() {
		return new ReissueUserCommand(accessToken, refreshToken);
	}
}
