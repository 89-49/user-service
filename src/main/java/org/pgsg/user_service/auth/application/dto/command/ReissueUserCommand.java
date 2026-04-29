package org.pgsg.user_service.auth.application.dto.command;

public record ReissueUserCommand(
		String accessToken,
		String refreshToken
) {
}
