package org.pgsg.user_service.user.application.dto.info;

import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.UUID;

public record LoginUserDetailInfo(
		UUID userId,
		String username,
		String password,
		UserRole userRole,
		String name,
		String nickname,
		boolean isEnabled
) {
	public static LoginUserDetailInfo from(User user) {
		return new LoginUserDetailInfo(
				user.getUserId(),
				user.getUsername(),
				user.getPassword(),
				user.getUserRole(),
				user.getName(),
				user.getNickname(),
				user.isEnabled()
		);
	}
	public UserDetailsImpl toUserDetails() {
		return UserDetailsImpl.builder()
				.uuid(userId)
				.username(username)
				.password(password)  // 재발급 시 불필요
				.userRole(userRole.getRole())
				.name(name)
				.nickname(nickname)
				.enabled(isEnabled)
				.build();
	}
}
