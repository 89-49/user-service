package org.pgsg.user_service.user.application.dto;

import org.pgsg.user_service.user.domain.entity.User;
import org.pgsg.user_service.user.domain.entity.UserRole;

import java.util.UUID;

// 인증
public record UserDetailsInfo(
		UUID userId,
		String username,
		String password,
		UserRole userRole,
		String name,
		String nickname,
		boolean isEnabled
) {
	// User 엔티티 -> UserDetailInfo DTO
	public static UserDetailsInfo from(User user) {
		return new UserDetailsInfo(
				user.getUserId(),
				user.getUsername(),
				user.getPassword(),
				user.getUserRole(),
				user.getName(),
				user.getNickname(),
				user.isEnabled()
		);
	}
}
