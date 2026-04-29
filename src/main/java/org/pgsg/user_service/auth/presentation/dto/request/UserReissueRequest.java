package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.pgsg.user_service.auth.application.dto.command.ReissueUserCommand;

public record UserReissueRequest(
		@NotNull
		String accessToken,
		@NotNull
		String refreshToken
) {
	public ReissueUserCommand toCommand() {
		return new ReissueUserCommand(accessToken, refreshToken);
	}
}
