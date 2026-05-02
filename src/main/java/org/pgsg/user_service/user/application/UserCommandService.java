package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserAdminCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserSelfCommand;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;

	@Transactional
	public User createUser(CreateUserCommand createdUserCommand) {
		if (userRepository.existsByUsername(createdUserCommand.username())) {
			throw new UserServiceException(UserErrorCode.DUPLICATE_USER);
		}

		try {
			return userRepository.save(createdUserCommand.toUserEntity());
		} catch (DuplicateKeyException e) {
			throw new UserServiceException(UserErrorCode.DUPLICATE_USER);
		} catch (DataIntegrityViolationException e) {
			throw new UserServiceException(UserErrorCode.SAVE_FAILURE);
		}
	}

	@Transactional
	public User updateUserProfile(UpdateUserSelfCommand updateCommand) {
		User targetUser = userRepository.findById(updateCommand.userId())
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		targetUser.updateProfile(updateCommand.name(), updateCommand.nickname());
		targetUser.updatePassword(updateCommand.password());
		targetUser.updateChatTimeRanges(updateCommand.toChatTimeRangeList());

		// TODO: 회원정보 수정 완료 -> 다른 도메인에 저장된 회원 정보를 업데이트하기 위한 이벤트 발행

		return targetUser;
	}

	@Transactional
	public User updateUserByAdmin(UpdateUserAdminCommand updateCommand) {
		User targetUser = userRepository.findById(updateCommand.userId())
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		targetUser.updateProfile(updateCommand.name(), updateCommand.nickname());
		targetUser.updateRole(updateCommand.userRole());

		// TODO: 회원정보 수정 완료 -> 다른 도메인에 저장된 회원 정보를 업데이트하기 위한 이벤트 발행

		return targetUser;
	}

	@Transactional
	public User deleteUser(UUID userId) {
		User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		targetUser.clearChatTimeRanges();
		targetUser.delete(userId);

		return targetUser;
	}
}
