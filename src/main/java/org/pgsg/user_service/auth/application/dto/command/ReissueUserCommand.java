package org.pgsg.user_service.auth.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReissueUserCommand(
		String accessToken,
		String refreshToken
) {
}
