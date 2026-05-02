package org.pgsg.user_service.user.presentation.dto.request;

import org.pgsg.user_service.user.application.dto.command.UpdateUserCommand;

import java.util.UUID;

public record UserUpdateRequest(
		String name,
		String nickname
) {
	public UpdateUserCommand toCommand(UUID userId) {
		return new UpdateUserCommand(
				userId,
				name(),
				nickname()
		);
	}
}
