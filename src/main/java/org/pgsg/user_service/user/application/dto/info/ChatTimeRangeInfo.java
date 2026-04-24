package org.pgsg.user_service.user.application.dto.info;

import org.pgsg.user_service.user.domain.entity.ChatTimeRange;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ChatTimeRangeInfo(
		DayOfWeek dayOfWeek,
		LocalTime startTime,
		LocalTime endTime
) {
	public static ChatTimeRangeInfo from(ChatTimeRange chatTimeRange) {
		return new ChatTimeRangeInfo(
				chatTimeRange.getDayOfWeek(),
				chatTimeRange.getStartTime(),
				chatTimeRange.getEndTime()
		);
	}
}
