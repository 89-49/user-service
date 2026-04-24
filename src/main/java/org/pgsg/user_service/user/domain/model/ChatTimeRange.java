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
		return new ChatTimeRange(dayOfWeek, startTime, endTime);
	}
}
