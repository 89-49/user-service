package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// TODO: CQRS 패턴 도입
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RoleCheck roleCheck;

	@Transactional
	public UserDetailInfo createUser(CreateUserCommand createdUserCommand) {
		// TODO: GlobalExceptionHandler 도입 시 회원 도메인 전용 커스텀 예외 클래스로 대체
		if (userRepository.existsByUsername(createdUserCommand.username())) {
			throw new IllegalArgumentException("이미 등록된 회원입니다.");
		}
		// TODO: GlobalExceptionHandler 도입 시 회원 도메인 전용 커스텀 예외 클래스로 대체
		if (UserRole.isAdmin(createdUserCommand.userRole()) && !roleCheck.hasRole(UserRole.MASTER)) {
			throw new AccessDeniedException("관리자를 등록할 권한이 없습니다.");
		}

		User newUser = createdUserCommand.toUserEntity();
		User savedUser = userRepository.save(newUser);

		return UserDetailInfo.from(savedUser);
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUserForAdmin(UUID userId) {
		// TODO: GlobalExceptionHandler 도입 시 회원 도메인 전용 커스텀 예외 클래스로 대체
		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))) {
			throw new AccessDeniedException("해당 리소스에 접근할 관리자 권한이 없습니다.");
		}

		return getUser(userId);
	}

	@Transactional(readOnly = true)
	public UserDetailInfo getUser(UUID userId) {
		User user = userRepository.findById(userId)
				// TODO: GlobalExceptionHandler 도입 시 회원 도메인용 커스텀 예외 클래스로 대체
				.orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 회원을 찾을 수 없습니다."));

		return UserDetailInfo.from(user);
	}

	@Transactional(readOnly = true)
	public LoginUserDetailInfo getUser(String username) {
		User user = userRepository.findByUsername(username)
				// TODO: GlobalExceptionHandler 도입 시 회원 도메인용 커스텀 예외 클래스로 대체
				.orElseThrow(() -> new IllegalArgumentException("해당 username을 사용하는 회원을 찾을 수 없습니다."));

		return LoginUserDetailInfo.from(user);
	}
}
