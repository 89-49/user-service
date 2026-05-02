package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandFacade {

	private final UserCommandService userCommandService;
	private final RoleCheck roleCheck;

	public UserDetailInfo createUser(CreateUserCommand createUserCommand) {
		UserRole newUserRole = createUserCommand.userRole();

		if (roleCheck.checkUserAdmin(newUserRole) && !roleCheck.hasRole(UserRole.MASTER)) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED_ROLE_ASSIGNMENT);
		}

		User user = userCommandService.createUser(createUserCommand);

		return UserDetailInfo.from(user);
	}
}
