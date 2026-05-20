package org.pgsg.user_service.user.presentation.dto.response;

import org.pgsg.user_service.user.application.dto.info.ChatTimeRangeInfo;

import java.util.List;

public record UserChatTimeListResponse(
		List<ChatTimeRangeInfo> chatTimeRanges
) {
	public static UserChatTimeListResponse from(List<ChatTimeRangeInfo> chatTimeRanges) {
		return new UserChatTimeListResponse(chatTimeRanges);
	}
}
