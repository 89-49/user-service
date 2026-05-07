package org.pgsg.user_service.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
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
    @DisplayName("delete() 호출 시 사용자가 비활성화되어야 한다")
    void delete_deactivatesUser() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        user.delete(UUID.randomUUID());
        assertThat(user.isEnabled()).isFalse();
    }
}
