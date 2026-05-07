package org.pgsg.user_service.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
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
    }

    @Test
    @DisplayName("updateRole()은 사용자 역할을 업데이트해야 한다")
    void updateRole_updatesUserRole() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");

        user.updateRole(UserRole.MANAGER);

        assertThat(user.getUserRole()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    @DisplayName("updatePassword()는 비밀번호를 업데이트해야 한다")
    void updatePassword_updatesPassword() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");

        user.updatePassword("newPassword");

        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("chatTimeRanges 관련 메서드들이 정상 동작해야 한다")
    void chatTimeRanges_methodsWorkCorrectly() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        ChatTimeRange range1 = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        ChatTimeRange range2 = ChatTimeRange.of(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));

        // addChatTimeRangeList
        user.addChatTimeRangeList(List.of(range1));
        assertThat(user.getChatTimeRanges()).hasSize(1).contains(range1);

        // updateChatTimeRanges
        user.updateChatTimeRanges(List.of(range2));
        assertThat(user.getChatTimeRanges()).hasSize(1).contains(range2);

        // clearChatTimeRanges
        user.clearChatTimeRanges();
        assertThat(user.getChatTimeRanges()).isEmpty();
    }

    @Test
    @DisplayName("delete() 호출 시 사용자가 비활성화되어야 한다")
    void delete_deactivatesUser() {
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        UUID actorId = UUID.randomUUID();

        assertThat(user.isEnabled()).isTrue();

        user.delete(actorId);

        assertThat(user.isEnabled()).isFalse();
    }
}
