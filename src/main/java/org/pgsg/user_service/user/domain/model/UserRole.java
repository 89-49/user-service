package org.pgsg.user_service.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum UserRole {

	USER("일반 사용자"),
	MANAGER("관리자"),
	MASTER("총관리자");

	private final String description;

	public String getRole() {
		return "ROLE_" + this.name();
	}

	public static boolean isAdmin(UserRole userRole) {
		return userRole == MANAGER || userRole == MASTER;
	}

	public static Optional<UserRole> of(String userRoleInfo) {
		if (userRoleInfo == null || userRoleInfo.isBlank()) {
			return Optional.empty();
		}
		String trimmedInfo = userRoleInfo.trim();
		return Arrays.stream(UserRole.values())
				.filter(role -> role.isMatched(trimmedInfo))
				.findAny();
	}

	public static UserRole find(String userRoleInfo) {
		return UserRole.of(userRoleInfo)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_ROLE_NOT_FOUND));
	}

	private boolean isMatched(String roleInfo) {
		return this.name().equalsIgnoreCase(roleInfo)
				|| this.getRole().equalsIgnoreCase(roleInfo)
				|| this.description.equals(roleInfo);
	}
}
