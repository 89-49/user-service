package org.pgsg.user_service.user.application.dto.info;

import org.pgsg.user_service.user.domain.entity.User;
import org.pgsg.user_service.user.domain.entity.UserRole;

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
}
