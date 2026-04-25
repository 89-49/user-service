package org.pgsg.user_service.user.domain.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatTimeRange {

	@Column(name = "day_of_week", nullable = false)
	private DayOfWeek dayOfWeek;

	// 요일별 채팅가능 시간이므로 날짜 정보 없이 시간만 저장
	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	public static ChatTimeRange of(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
		validateChatTime(dayOfWeek, startTime, endTime);
		return new ChatTimeRange(dayOfWeek, startTime, endTime);
	}

	private static void validateChatTime(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
		if (dayOfWeek == null || startTime == null || endTime == null) {
			throw new IllegalArgumentException("요일/시작 시간/종료 시간은 필수입니다.");
		}
		if (!startTime.isBefore(endTime)) {
			throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
		}
	}
}
