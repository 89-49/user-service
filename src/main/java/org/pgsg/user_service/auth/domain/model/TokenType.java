package org.pgsg.user_service.auth.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
	ACCESS("access"),
	REFRESH("refresh");

	private final String value;

	public boolean matches(String value) {
		return this.value.equals(value);
	}
}
