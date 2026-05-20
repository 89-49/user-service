package org.pgsg.user_service.user.application.dto.query;

import java.time.LocalDate;
import java.time.LocalTime;

public record SearchChatTimeQuery(
		LocalDate chatDate,
		LocalTime chatTime
) {
}
