package org.pgsg.user_service.user.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.pgsg.user_service.user.application.dto.command.CreateChatTimeCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserSelfCommand;

import java.util.List;
import java.util.UUID;

public record UserSelfUpdateRequest(
		@Size(max = 20)
		String name,

		@Size(max = 20)
		String nickname,

		@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$",
				message = "[user.validation.user-info-password.invalid-pattern]")
		String password,

		List<@NotNull @Valid ChatTimeRequest> chatTimeRanges
) {
	public List<CreateChatTimeCommand> chatTimeRangeCommand() {
		return chatTimeRanges != null
				? chatTimeRanges.stream().map(ChatTimeRequest::toCommand).toList()
				: null;
	}

	public UpdateUserSelfCommand toCommand(UUID userId) {
		return new UpdateUserSelfCommand(
				userId,
				name,
				nickname,
				password,
				chatTimeRangeCommand()
		);
	}
}
