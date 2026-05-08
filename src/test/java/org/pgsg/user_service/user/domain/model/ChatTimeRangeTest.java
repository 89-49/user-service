package org.pgsg.user_service.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatTimeRangeTest {

    @Test
    @DisplayName("of()는 올바른 인자가 주어지면 ChatTimeRange 객체를 생성해야 한다")
    void of_createsChatTimeRangeForValidArgs() {
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);

        ChatTimeRange range = ChatTimeRange.of(day, start, end);

        assertThat(range.getDayOfWeek()).isEqualTo(day);
        assertThat(range.getStartTime()).isEqualTo(start);
        assertThat(range.getEndTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("of()는 필수 인자가 누락되면 UserServiceException을 던져야 한다")
    void of_throwsExceptionForMissingArgs() {
        assertThatThrownBy(() -> ChatTimeRange.of(null, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.CHAT_TIME_DAY_OF_WEEK_REQUIRED);

        assertThatThrownBy(() -> ChatTimeRange.of(DayOfWeek.MONDAY, null, LocalTime.of(10, 0)))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.CHAT_TIME_START_TIME_REQUIRED);

        assertThatThrownBy(() -> ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), null))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.CHAT_TIME_END_TIME_REQUIRED);
    }

    @Test
    @DisplayName("of()는 시작 시간이 종료 시간보다 늦거나 같으면 UserServiceException을 던져야 한다")
    void of_throwsExceptionForInvalidTimeRange() {
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(9, 0);
        LocalTime same = LocalTime.of(10, 0);

        assertThatThrownBy(() -> ChatTimeRange.of(DayOfWeek.MONDAY, start, end))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_CHAT_TIME_RANGE);

        assertThatThrownBy(() -> ChatTimeRange.of(DayOfWeek.MONDAY, same, same))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_CHAT_TIME_RANGE);
    }

    @Test
    @DisplayName("overlapsWith()는 요일이 다르면 false를 반환해야 한다")
    void overlapsWith_returnsFalseForDifferentDays() {
        ChatTimeRange mon = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ChatTimeRange tue = ChatTimeRange.of(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThat(mon.overlapsWith(tue)).isFalse();
    }

    @Test
    @DisplayName("overlapsWith()는 같은 요일이고 시간이 겹치면 true를 반환해야 한다")
    void overlapsWith_returnsTrueForOverlappingTimes() {
        ChatTimeRange base = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));

        // 부분 겹침 (앞쪽)
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)))).isTrue();
        // 부분 겹침 (뒤쪽)
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(13, 0)))).isTrue();
        // 포함 관계
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))).isTrue();
        // 동일 범위
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)))).isTrue();
    }

    @Test
    @DisplayName("overlapsWith()는 같은 요일이라도 시간이 겹치지 않으면 false를 반환해야 한다")
    void overlapsWith_returnsFalseForNonOverlappingTimes() {
        ChatTimeRange base = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));

        // 완전히 앞쪽
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0)))).isFalse();
        // 완전히 뒤쪽
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 0)))).isFalse();
        // 경계선에서 만남 (겹치지 않음)
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)))).isFalse();
        assertThat(base.overlapsWith(ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0)))).isFalse();
    }
}
