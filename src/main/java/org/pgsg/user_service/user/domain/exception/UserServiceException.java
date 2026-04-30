package org.pgsg.user_service.user.domain.exception;

import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorCode;

public class UserServiceException extends CustomException {

	public UserServiceException(ErrorCode errorCode) {
		super(errorCode, null);
	}

	public UserServiceException(ErrorCode errorCode, String field) {
		super(errorCode, field);
	}

	public UserServiceException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, null);
		this.initCause(cause);
	}
}
