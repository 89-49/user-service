package org.pgsg.user_service.user.application.dto.command;

import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.UUID;

public record UpdateUserAdminCommand(
		UUID userId,
		String name,
		String nickname,
		UserRole userRole
) {
}
