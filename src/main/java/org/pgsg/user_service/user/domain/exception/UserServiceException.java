package org.pgsg.user_service.user.domain.exception;

import org.pgsg.common.exception.CustomException;

public class UserServiceException extends CustomException {

	public UserServiceException(String errorName) {
		super(errorName, null);
	}

	public UserServiceException(String errorName, String field) {
		super(errorName, field);
	}
}
