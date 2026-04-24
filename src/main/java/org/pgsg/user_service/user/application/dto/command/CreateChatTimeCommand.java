package org.pgsg.user_service.user.application.dto.command;

import org.pgsg.user_service.user.domain.model.ChatTimeRange;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateChatTimeCommand(
		DayOfWeek dayOfWeek,
		LocalTime startTime,
		LocalTime endTime
) {
	public ChatTimeRange to() {
		return ChatTimeRange.of(dayOfWeek, startTime, endTime);
	}
}
