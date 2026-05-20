package org.pgsg.user_service.user.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserErrorCodeTest {

    @Test
    @DisplayName("fromErrorKey()는 올바른 errorKey가 주어지면 해당 UserErrorCode를 반환해야 한다")
    void fromErrorKey_returnsCorrectErrorCode() {
        String errorKey = "[user.exception.not-found.user]";
        UserErrorCode errorCode = UserErrorCode.fromErrorKey(errorKey);

        assertThat(errorCode).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("fromErrorKey()는 존재하지 않는 errorKey가 주어지면 null을 반환해야 한다")
    void fromErrorKey_returnsNullForInvalidKey() {
        assertThat(UserErrorCode.fromErrorKey("INVALID_KEY")).isNull();
    }
}
