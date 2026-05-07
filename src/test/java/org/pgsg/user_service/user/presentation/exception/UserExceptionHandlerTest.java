package org.pgsg.user_service.user.presentation.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.common.response.ErrorResponse;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
class UserExceptionHandlerTest {

    @Autowired
    private UserExceptionHandler userExceptionHandler;

    @Test
    @DisplayName("UserServiceException - 등록된 에러 코드(U017) 발생 시 정상 응답을 반환해야 한다")
    void handleUserException_Success() {
        UserServiceException exception = new UserServiceException(UserErrorCode.USER_NOT_FOUND);
        ResponseEntity<ErrorResponse> response = userExceptionHandler.handleUserException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(Objects.requireNonNull(response.getBody()).code()).isEqualTo("U017");
        assertThat(response.getBody().message().toString()).contains("해당 회원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("UserServiceException - 정의되지 않은 에러 키의 경우 500 에러를 반환해야 한다")
    void handleUserException_UndefinedKey() {
        // 정의되지 않은 임의의 예외 시뮬레이션 (CustomException 상속 객체 등)
        UserServiceException exception = mock(UserServiceException.class);
        when(exception.getErrorCode()).thenReturn(() -> "UNDEFINED_KEY");

        ResponseEntity<ErrorResponse> response = userExceptionHandler.handleUserException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(Objects.requireNonNull(response.getBody()).code()).isEqualTo("SYSTEM-500");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException - 필드 에러 발생 시 UserErrorCode 매핑을 확인한다")
    void handleValidationException_FieldUserErrorCode() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "username", "[user.validation.user-info-id.required]");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<ErrorResponse> response = userExceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(Objects.requireNonNull(response.getBody()).code()).isEqualTo("U001");
        assertThat(response.getBody().field()).isEqualTo("username");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException - 미매핑 필드 에러도 400 응답을 반환해야 한다")
    void handleValidationException_GeneralFieldError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "이메일 형식이 올바르지 않습니다.");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<ErrorResponse> response = userExceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
