package org.pgsg.user_service.user.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.pgsg.user_service.user.application.dto.command.CreateChatTimeCommand;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ChatTimeRequest(
		@NotNull(message = "[user.validation.user-info-chat-time.day-of-week-required]")
		DayOfWeek dayOfWeek,

		@NotNull(message = "[user.validation.user-info-chat-time.start-time-required]")
		LocalTime startTime,

		@NotNull(message = "[user.validation.user-info-chat-time.end-time-required]")
		LocalTime endTime
) {
	public CreateChatTimeCommand toCommand() {
		return new CreateChatTimeCommand(dayOfWeek, startTime, endTime);
	}
}
