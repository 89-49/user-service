package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// TODO: CQRS 패턴 도입
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RoleCheck roleCheck;

	@Transactional
	public UserDetailInfo createUser(CreateUserCommand createdUserCommand) {
		if (userRepository.existsByUsername(createdUserCommand.username())) {
			throw new UserServiceException(UserErrorCode.DUPLICATE_USERNAME);
		}
		if (UserRole.isAdmin(createdUserCommand.userRole()) && !roleCheck.hasRole(UserRole.MASTER)) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED_ROLE_ASSIGNMENT);
		}

		try {
			User savedUser = userRepository.save(createdUserCommand.toUserEntity());

			return UserDetailInfo.from(savedUser);
		} catch (DataIntegrityViolationException e) {
			String message = e.getMostSpecificCause().getMessage();
			if (message != null && message.contains("uk_user_username")) {
				throw new UserServiceException(UserErrorCode.DUPLICATE_USERNAME, e);
			}
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUserForAdmin(UUID userId) {

		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))) {
			throw new UserServiceException(UserErrorCode.ADMIN_ACCESS_DENIED);
		}

		return getUser(userId);
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		return UserDetailInfo.from(user);
	}

	@Transactional(readOnly = true)
	public LoginUserDetailInfo getUserForAuth(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		return LoginUserDetailInfo.from(user);
	}

	@Transactional(readOnly = true)
	public LoginUserDetailInfo getUserForAuth(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND, "username"));

		return LoginUserDetailInfo.from(user);
	}
}
