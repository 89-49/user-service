package org.pgsg.user_service.user.domain.service;

import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.List;

public interface RoleCheck {

	boolean hasRole(UserRole userRoles);
	boolean hasRole(List<UserRole> userRoles);
}
