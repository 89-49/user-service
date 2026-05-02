package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserAdminCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserSelfCommand;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.result.UserDeleteResult;
import org.pgsg.user_service.user.application.dto.result.UserUpdateResult;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandFacade {

	private final UserCommandService userCommandService;
	private final RoleCheck roleCheck;
	private final PasswordEncoder passwordEncoder;

	public UserDetailInfo createUser(CreateUserCommand createUserCommand) {
		UserRole newUserRole = createUserCommand.userRole();

		if (roleCheck.checkUserAdmin(newUserRole) && !roleCheck.hasRole(UserRole.MASTER)) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED_ROLE_ASSIGNMENT);
		}

		User user = userCommandService.createUser(createUserCommand);

		return UserDetailInfo.from(user);
	}

	public UserUpdateResult updateMyProfile(UpdateUserSelfCommand command) {
		if (!roleCheck.checkUserSelf(command.userId())) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED_USER_UPDATE);
		}

		String encryptedPassword = null;
		if (command.password() != null && !command.password().isBlank()) {
			encryptedPassword = passwordEncoder.encode(command.password());
		}

		UpdateUserSelfCommand updatedCommand = new UpdateUserSelfCommand(
				command.userId(),
				command.name(),
				command.nickname(),
				encryptedPassword,
				command.chatTimeRanges()
		);

		User updatedUser = userCommandService.updateUserProfile(updatedCommand);

		return UserUpdateResult.from(updatedUser);
	}

	public UserUpdateResult updateUserByAdmin(UpdateUserAdminCommand command) {
		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED_USER_UPDATE);
		}

		User updatedUser = userCommandService.updateUserByAdmin(command);

		return UserUpdateResult.from(updatedUser);
	}

	public UserDeleteResult deleteUser(UUID userId) {
		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER)) && !roleCheck.checkUserSelf(userId)) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED);
		}

		// TODO: 현재 구매 진행 중인 상품이 있는지 검증하는 로직 추가

		// TODO: 상품 목록 조회 시, 현재 예약이나 거래 진행 중인 상품이 있는지 검증하는 로직 추가

		User user = userCommandService.deleteUser(userId);

		return UserDeleteResult.from(user);
	}
}
