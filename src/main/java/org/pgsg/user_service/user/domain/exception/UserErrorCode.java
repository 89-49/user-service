package org.pgsg.user_service.user.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 유효성 검사
    USER_INFO_ID_REQUIRED("[user.validation.user-info-id.required]", "username"),
    USER_INFO_ID_INVALID_FORMAT("[user.validation.user-info-id.invalid-format]", "username"),
    USER_INFO_PASSWORD_REQUIRED("[user.validation.user-info-password.required]", "password"),
    USER_INFO_PASSWORD_INVALID_PATTERN("[user.validation.user-info-password.invalid-pattern]", "password"),
    USER_INFO_ROLE_REQUIRED("[user.validation.user-info-role.required]", "userRole"),
    USER_INFO_ROLE_INVALID_PATTERN("[user.validation.user-info-role.invalid-pattern]", "userRole"),
    USER_INFO_NAME_REQUIRED("[user.validation.user-info-name.required]", "name"),
    USER_INFO_NAME_INVALID_PATTERN("[user.validation.user-info-name.invalid-pattern]", "name"),
    USER_INFO_NICKNAME_REQUIRED("[user.validation.user-info-nickname.required]", "nickname"),
    USER_INFO_NICKNAME_INVALID_PATTERN("[user.validation.user-info-nickname.invalid-pattern]", "nickname"),
    
    CHAT_TIME_RANGE_REQUIRED("[user.validation.user-info-chat-time.range-required]", "chatTimeRanges"),
    CHAT_TIME_DAY_OF_WEEK_REQUIRED("[user.validation.user-info-chat-time.day-of-week-required]", "dayOfWeek"),
    CHAT_TIME_START_TIME_REQUIRED("[user.validation.user-info-chat-time.start-time-required]", "startTime"),
    CHAT_TIME_END_TIME_REQUIRED("[user.validation.user-info-chat-time.end-time-required]", "endTime"),

    // 토큰 재발급 유효성
    REISSUE_ACCESS_TOKEN_REQUIRED("[user.validation.reissue.access-token-required]", "accessToken"),
    REISSUE_REFRESH_TOKEN_REQUIRED("[user.validation.reissue.refresh-token-required]", "refreshToken"),

    // 서비스 예외
    ADMIN_ACCESS_DENIED("[user.exception.access-denied.read-user-info]", "role"),
    UNAUTHORIZED_ROLE_ASSIGNMENT("[user.exception.access-denied.manager-assignment]", "userRole"),
    USER_NOT_FOUND("[user.exception.not-found.user]", "userId"),
    DUPLICATE_USERNAME("[user.exception.conflict.duplicate-username]", "username"),
    USER_ROLE_NOT_FOUND("[user.exception.not-found.user-role]", "userRole"),
    UNAUTHORIZED("[user.exception.unauthorized.invalid-user]", "authentication");

    private final String errorKey;
    @Getter
	private final String field;

    public static UserErrorCode fromErrorKey(String errorKey) {
        for (UserErrorCode errorCode : values()) {
            if (errorCode.getErrorKey().equals(errorKey)) {
                return errorCode;
            }
        }
        return null;
    }

}
