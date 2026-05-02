package org.pgsg.user_service.user.application.dto.command;

import java.util.UUID;

public record UpdateUserCommand(
		UUID userId,
		String name,
		String nickname
) {

}
