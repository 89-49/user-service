package org.pgsg.user_service.user.application.dto.command;

import org.pgsg.user_service.user.domain.model.ChatTimeRange;

import java.util.List;
import java.util.UUID;

public record UpdateUserSelfCommand(
		UUID userId,
		String name,
		String nickname,
		String password,
		List<CreateChatTimeCommand> chatTimeRanges
) {
	public List<ChatTimeRange> toChatTimeRangeList() {
		if (chatTimeRanges == null) {
			return null;
		}
		return chatTimeRanges.stream()
				.map(CreateChatTimeCommand::toChatTime)
				.toList();
	}
}
