package org.pgsg.user_service.user.presentation.dto.request;

import jakarta.validation.constraints.Size;
import org.pgsg.user_service.user.application.dto.command.UpdateUserAdminCommand;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.UUID;

public record UserAdminUpdateRequest(
		@Size(max = 20)
		String name,
		@Size(max = 20)
		String nickname,
		String userRole
) {
	public UpdateUserAdminCommand toCommand(UUID userId) {
		return new UpdateUserAdminCommand(
				userId,
				name,
				nickname,
				userRole != null ? UserRole.find(userRole) : null
		);
	}
}
