package org.pgsg.user_service.user.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("create()는 새로운 User 객체를 생성해야 한다")
    void create_createsNewUser() {
        User user = User.create("testuser", "encryptedPassword", UserRole.USER, "홍길동", "길동이");

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encryptedPassword");
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getNickname()).isEqualTo("길동이");
    }

    @Test
    @DisplayName("updateProfile()은 이름과 닉네임을 업데이트해야 한다")
    void updateProfile_updatesNameAndNickname() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.updateProfile("새이름", "새닉네임");
        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("updateProfile()은 null이 전달되면 해당 필드를 유지해야 한다")
    void updateProfile_ignoresNullValues() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        
        user.updateProfile(null, "새닉네임");
        assertThat(user.getName()).isEqualTo("이름");
        assertThat(user.getNickname()).isEqualTo("새닉네임");

        user.updateProfile("새이름", null);
        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getNickname()).isEqualTo("새닉네임");

        user.updateProfile(null, null);
        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("updateRole()은 사용자 역할을 업데이트해야 한다")
    void updateRole_updatesUserRole() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.updateRole(UserRole.MANAGER);
        assertThat(user.getUserRole()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    @DisplayName("updateRole()은 null이 전달되면 역할을 유지해야 한다")
    void updateRole_ignoresNullValue() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.updateRole(null);
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("updatePassword()는 비밀번호를 업데이트해야 한다")
    void updatePassword_updatesPassword() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.updatePassword("newPassword");
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("updatePassword()는 null이 전달되면 비밀번호를 유지해야 한다")
    void updatePassword_ignoresNullValue() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.updatePassword(null);
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    @DisplayName("isEnabled()는 deletedAt이 null일 때 true를, 아니면 false를 반환해야 한다")
    void isEnabled_checksDeletedAt() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        
        // 초기 상태 (deletedAt is null)
        assertThat(user.isEnabled()).isTrue();

        // 삭제 처리 시 (deletedAt is not null)
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("updateChatTimeRanges()는 기존 목록을 비우고 새로운 목록으로 교체해야 한다")
    void updateChatTimeRanges_replacesList() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));

        user.addChatTimeRangeList(List.of(range1));
        assertThat(user.getChatTimeRanges()).hasSize(1);

        user.updateChatTimeRanges(List.of(range2));
        assertThat(user.getChatTimeRanges()).hasSize(1).containsExactly(range2);
        
        user.updateChatTimeRanges(null); // null 시 유지
        assertThat(user.getChatTimeRanges()).hasSize(1).containsExactly(range2);
    }

    @Test
    @DisplayName("중복되지 않는 시작 시간을 가졌더라도 겹치는 시간대는 예외를 던져야 한다")
    void addChatTimeRangeList_throwsExceptionForOverlappingRanges() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));

        Assertions.assertThatThrownBy(() -> user.addChatTimeRangeList(List.of(range1, range2)))
                .isInstanceOf(org.pgsg.user_service.user.domain.exception.UserServiceException.class);
    }

    @Test
    @DisplayName("updateChatTimeRanges() 호출 시 기존 시간대를 대체하므로 시작 시간이 같아도 성공해야 한다")
    void updateChatTimeRanges_succeedsWhenReplacingWithSameStartTime() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        user.addChatTimeRangeList(List.of(range1));

        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));

        org.assertj.core.api.Assertions.assertThatCode(() -> user.updateChatTimeRanges(List.of(range2)))
                .doesNotThrowAnyException();
        assertThat(user.getChatTimeRanges()).hasSize(1).containsExactly(range2);
    }

    @Test
    @DisplayName("findAvailableChatTime()은 날짜와 시간에 맞는 채팅 가능 시간대를 반환해야 한다")
    void findAvailableChatTime_returnsMatchingRanges() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(16, 0));
        user.addChatTimeRangeList(List.of(range1, range2));

        // 월요일 10시 -> range1 반환
        LocalDate monday = LocalDate.of(2024, 5, 13); // 2024-05-13은 월요일
        List<ChatTimeRange> result1 = user.findAvailableChatTime(monday, LocalTime.of(10, 0));
        assertThat(result1).hasSize(1).containsExactly(range1);

        // 월요일 11시 -> 끝나는 시간은 포함 안 됨 (empty)
        List<ChatTimeRange> result2 = user.findAvailableChatTime(monday, LocalTime.of(11, 0));
        assertThat(result2).isEmpty();

        // 화요일 10시 -> 요일 불일치 (empty)
        LocalDate tuesday = LocalDate.of(2024, 5, 14);
        List<ChatTimeRange> result3 = user.findAvailableChatTime(tuesday, LocalTime.of(10, 0));
        assertThat(result3).isEmpty();

        // 입력값이 null인 경우 -> (empty)
        assertThat(user.findAvailableChatTime(null, LocalTime.of(10, 0))).isEmpty();
        assertThat(user.findAvailableChatTime(monday, null)).isEmpty();
    }

    @Test
    @DisplayName("validateRanges()는 인접한 시간대가 겹치지 않으면(경계선 포함) 성공해야 한다")
    void validateRanges_succeedsForNonOverlappingConsecutiveRanges() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));

        // 10:00에 딱 붙어 있는 경우 허용되어야 함
        Assertions.assertThatCode(() -> user.addChatTimeRangeList(List.of(range1, range2)))
                .doesNotThrowAnyException();
        assertThat(user.getChatTimeRanges()).hasSize(2);
    }

    @Test
    @DisplayName("validateRanges()는 정렬되지 않은 상태로 들어와도 정렬 후 겹침을 체크해야 한다")
    void validateRanges_checksOverlapsAfterSorting() {
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ChatTimeRange overlapping = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));

        // [9-11]과 [10-12]가 겹침 (순서 상관 없이 검증되어야 함)
        Assertions.assertThatThrownBy(() -> user.addChatTimeRangeList(List.of(range1, range2, overlapping)))
                .isInstanceOf(org.pgsg.user_service.user.domain.exception.UserServiceException.class);
    }


    @Test
    @DisplayName("delete() 호출 시 사용자가 비활성화되어야 한다")
    void delete_deactivatesUser() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.delete(UUID.randomUUID());
        assertThat(user.isEnabled()).isFalse();
    }
}
