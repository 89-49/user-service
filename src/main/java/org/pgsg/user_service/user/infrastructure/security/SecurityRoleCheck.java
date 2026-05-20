package org.pgsg.user_service.user.infrastructure.security;

import org.pgsg.common.util.SecurityUtil;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Component
public class SecurityRoleCheck implements RoleCheck {

	// 현재 사용자가 파라미터에서 지정한 권한을 보유하는지 1:1 비교
	@Override
	public boolean hasRole(UserRole role) {
		return hasRole(List.of(role));
	}

	@Override
	public boolean hasRole(List<UserRole> allowedUserRoles) {

		return SecurityUtil.getCurrentUser()
				.map(userDetails -> {
					// 1. 현재 로그인한 사용자의 권한 불러오기
					String currentUserRoleInfo = userDetails.getUserRole();
					if (!StringUtils.hasText(currentUserRoleInfo)) {
						return false;
					}
					// 2. 현재 로그인한 사용자에 매핑되는 UserRole enum 조회
					UserRole currentUserRole = UserRole.find(currentUserRoleInfo);
					if (currentUserRole == UserRole.MASTER) {
						return true;
					}
					// 3. 현재 사용자의 권한이 파라미터에서 지정한 권한 중 하나에 해당되는지 확인
					return allowedUserRoles.contains(currentUserRole);
				})
				.orElse(false);
	}

	@Override
	public boolean checkUserAdmin(UserRole targetUserRole) {
		// 지정한 회원의 권한 검사용
		return UserRole.isAdmin(targetUserRole);
	}

	@Override
	public boolean checkUserSelf(UUID userId) {
		return SecurityUtil.getCurrentUserId()
				.map(currentUserId -> currentUserId.equals(userId))
				.orElse(false);
	}
}
