package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
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
			throw new UserServiceException("DuplicateUsernameException");
		}
		if (UserRole.isAdmin(createdUserCommand.userRole()) && !roleCheck.hasRole(UserRole.MASTER)) {
			throw new UserServiceException("UnauthorizedRoleAssignmentException");
		}

		try {
			User savedUser = userRepository.save(createdUserCommand.toUserEntity());

			return UserDetailInfo.from(savedUser);
		} catch (DataIntegrityViolationException e) {
			throw new UserServiceException("DuplicateUsernameException");
		}
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUserForAdmin(UUID userId) {

		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))) {
			throw new UserServiceException("AdminAccessDeniedException");
		}

		return getUser(userId);
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException("UserNotFoundException"));

		return UserDetailInfo.from(user);
	}

	@Transactional(readOnly = true)
	public LoginUserDetailInfo getUserForAuth(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException("UserNotFoundException"));

		return LoginUserDetailInfo.from(user);
	}

	@Transactional(readOnly = true)
	public LoginUserDetailInfo getUserForAuth(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UserServiceException("UserNotFoundException"));

		return LoginUserDetailInfo.from(user);
	}
}
