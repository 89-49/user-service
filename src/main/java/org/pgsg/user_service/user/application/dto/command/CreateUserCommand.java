package org.pgsg.user_service.user.application.dto.command;

import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.List;
import java.util.Objects;

public record CreateUserCommand(
		String username,
		String password,
		UserRole userRole,
		String name,
		String nickname,
		List<CreateChatTimeCommand> chatTimeRangeInfos
) {
	public List<ChatTimeRange> getChatTimeRanges() {
		if (chatTimeRangeInfos == null || chatTimeRangeInfos.isEmpty()) {
			return List.of();
		}
		return chatTimeRangeInfos.stream()
				.filter(Objects::nonNull)
				.map(CreateChatTimeCommand::toChatTime)
				.toList();
	}

	public User toUserEntity() {
		User newUser = User.create(username, password, userRole, name, nickname);
		newUser.addChatTimeRangeList(getChatTimeRanges());

		return newUser;
	}
}
