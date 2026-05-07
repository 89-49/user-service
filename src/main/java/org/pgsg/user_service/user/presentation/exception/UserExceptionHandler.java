package org.pgsg.user_service.user.presentation.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorConfigProperties;
import org.pgsg.common.exception.GlobalExceptionAdvice;
import org.pgsg.common.response.ErrorResponse;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class UserExceptionHandler implements GlobalExceptionAdvice {

	private final ErrorConfigProperties errorConfigProperties;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		if (fieldError == null) {
			String globalErrorKey = e.getBindingResult().getGlobalError() != null
					? e.getBindingResult().getGlobalError().getDefaultMessage() : null;

			return buildResponse(globalErrorKey, null, e);
		}
		String errorKey = fieldError.getDefaultMessage();

		UserErrorCode errorCode = UserErrorCode.fromErrorKey(errorKey);
		if (errorCode != null) {
			return buildResponse(errorCode.getErrorKey(), errorCode.getField(), e);
		}

		return buildResponse(errorKey, fieldError.getField(), e);
	}

	@ExceptionHandler(UserServiceException.class)
	public ResponseEntity<ErrorResponse> handleUserException(CustomException e) {
		String field = e.getField();
		if (field == null && e.getErrorCode() instanceof UserErrorCode uec) {
			field = uec.getField();
		}
		return buildResponse(e.getErrorCode().getErrorKey(), field, e);
	}

	/**
	 * 공통 에러 응답 빌더
	 */
	private ResponseEntity<ErrorResponse> buildResponse(String errorKey, String field, Exception e) {
		// [key] 형식에서 [] 제거
		String normalizedKey = (errorKey != null && errorKey.startsWith("[") && errorKey.endsWith("]"))
				? errorKey.substring(1, errorKey.length() - 1)
				: errorKey;

		ErrorConfigProperties.ErrorDetail detail = errorConfigProperties.getConfigs().get(normalizedKey);

		if (detail == null) {
			log.error("[TraceID: {}] Undefined Error Key: field={}, errorKey={}, normalizedKey={}",
					MDC.get("traceId"), field, errorKey, normalizedKey, e);

			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM-500", field, "정의되지 않은 서버 에러가 발생했습니다."));
		}

		log.error("[TraceID: {}] Exception: field={}, code={}, message={}",
				MDC.get("traceId"), field, detail.getCode(), detail.getMessage());

		HttpStatus status = HttpStatus.resolve(detail.getStatus());
		if (status == null) {
			log.warn("[TraceID: {}] Invalid HTTP Status Code in config: status={}, errorKey={}",
					MDC.get("traceId"), detail.getStatus(), normalizedKey);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		// ErrorResponse.of(status, code, field, message) 순서에 맞춰 수정
		return ResponseEntity
				.status(status)
				.body(ErrorResponse.of(status, detail.getCode(), field, detail.getMessage()));
	}
}
