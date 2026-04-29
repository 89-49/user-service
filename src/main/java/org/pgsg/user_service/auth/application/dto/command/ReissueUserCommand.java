package org.pgsg.user_service.auth.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReissueUserCommand(
		@NotNull
		@NotBlank
		String accessToken,

		@NotNull
		@NotBlank
		String refreshToken
) {
}
