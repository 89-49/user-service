package org.pgsg.user_service.user.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.pgsg.user_service.user.application.dto.query.SearchChatTimeQuery;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record UserChatTimeSearchRequest(
		@NotNull(message = "[user.validation.chat-availability.chat-date-required]")
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate chatDate,

		@NotNull(message = "[user.validation.chat-availability.chat-time-required]")
		@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
		LocalTime chatTime
) {
	public SearchChatTimeQuery toQuery() {
		return new SearchChatTimeQuery(chatDate(), chatTime());
	}
}
