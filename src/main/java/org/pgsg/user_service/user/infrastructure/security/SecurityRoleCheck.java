package org.pgsg.user_service.user.infrastructure.security;

import org.pgsg.user_service.user.domain.entity.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityRoleCheck implements RoleCheck {

	// 현재 사용자가 파라미터에서 지정한 권한을 보유하는지 1:1 비교
	@Override
	public boolean hasRole(UserRole role) {
		return hasRole(List.of(role));
	}

	@Override
	public boolean hasRole(List<UserRole> userRoles) {
		// TODO: 공통모듈의 SecurityUtil을 통해 가져온 사용자의 권한이 userRoles 중에 포함되어 있는지 확인하는 로직 구현
		return true;
	}
}
