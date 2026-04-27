package org.pgsg.user_service.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.util.Arrays;

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

	public static UserRole find(String userRoleInfo) {
		if (userRoleInfo == null || userRoleInfo.isBlank()) {
			throw new IllegalArgumentException("권한 정보가 비어있습니다.");
		}

		String trimmedInfo = userRoleInfo.trim();

		return Arrays.stream(UserRole.values())
				.filter(role -> role.isMatched(trimmedInfo))
				.findAny()
				.orElseThrow(() -> new UserServiceException("UserRoleNotFoundException"));
	}

	private boolean isMatched(String roleInfo) {
		return this.name().equalsIgnoreCase(roleInfo)
				|| this.getRole().equalsIgnoreCase(roleInfo)
				|| this.description.equals(roleInfo);
	}
}
