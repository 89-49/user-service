package org.pgsg.user_service.user.domain.model;

import java.util.Arrays;

public enum UserRole {

	USER,
	MANAGER,
	MASTER;

	public String getRole() {
		return "ROLE_" + this.name();
	}

	public static boolean isAdmin(UserRole userRole) {
		return userRole == MANAGER || userRole == MASTER;
	}

	public static UserRole find(String userRole) {
		return Arrays.stream(UserRole.values())
				.filter(role -> role.getRole().equals(userRole))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("해당하는 회원권한을 찾을 수 없습니다."));	// 현재는 임시로 IllegalArgumentException 사용, 커스텀 예외 클래스 추가 시 교체 예정
	}
}
