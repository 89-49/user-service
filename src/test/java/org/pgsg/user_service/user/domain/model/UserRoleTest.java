package org.pgsg.user_service.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRoleTest {

    @Test
    @DisplayName("getRole()은 ROLE_ 접두사가 붙은 이름을 반환해야 한다")
    void getRole_returnsNameWithRolePrefix() {
        assertThat(UserRole.USER.getRole()).isEqualTo("ROLE_USER");
        assertThat(UserRole.MANAGER.getRole()).isEqualTo("ROLE_MANAGER");
        assertThat(UserRole.MASTER.getRole()).isEqualTo("ROLE_MASTER");
    }

    @Test
    @DisplayName("isAdmin()은 MANAGER 또는 MASTER일 때 true를 반환해야 한다")
    void isAdmin_returnsTrueForManagerOrMaster() {
        assertThat(UserRole.isAdmin(UserRole.USER)).isFalse();
        assertThat(UserRole.isAdmin(UserRole.MANAGER)).isTrue();
        assertThat(UserRole.isAdmin(UserRole.MASTER)).isTrue();
    }

    @Nested
    @DisplayName("of() 메서드 테스트")
    class OfTest {

        @ParameterizedTest
        @ValueSource(strings = {"USER", "user", " ROLE_USER ", "일반 사용자"})
        @DisplayName("성공: 다양한 형태의 문자열로 UserRole을 찾을 수 있다")
        void of_success(String input) {
            Optional<UserRole> result = UserRole.of(input);
            assertThat(result).isPresent().contains(UserRole.USER);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "", "  "})
        @DisplayName("실패: 유효하지 않은 문자열은 Empty Optional을 반환한다")
        void of_failure_invalidString(String input) {
            assertThat(UserRole.of(input)).isEmpty();
        }

        @Test
        @DisplayName("실패: null 입력 시 Empty Optional을 반환한다")
        void of_failure_null() {
            assertThat(UserRole.of(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("find() 메서드 테스트")
    class FindTest {

        @Test
        @DisplayName("성공: 유효한 문자열 입력 시 UserRole을 반환한다")
        void find_success() {
            assertThat(UserRole.find("MANAGER")).isEqualTo(UserRole.MANAGER);
            assertThat(UserRole.find("관리자")).isEqualTo(UserRole.MANAGER);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 역할일 경우 USER_ROLE_NOT_FOUND 예외를 던진다")
        void find_failure_notFound() {
            assertThatThrownBy(() -> UserRole.find("GUEST"))
                    .isInstanceOf(UserServiceException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ROLE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 입력이 null이거나 공백일 경우 USER_ROLE_NOT_FOUND 예외를 던진다")
        void find_failure_nullOrBlank() {
            assertThatThrownBy(() -> UserRole.find(null))
                    .isInstanceOf(UserServiceException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ROLE_NOT_FOUND);

            assertThatThrownBy(() -> UserRole.find("  "))
                    .isInstanceOf(UserServiceException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ROLE_NOT_FOUND);
        }
    }
}
