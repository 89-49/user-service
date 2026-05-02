package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserCommand;
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
	public User updateUser(UpdateUserCommand updateCommand) {
		User targetUser = userRepository.findById(updateCommand.userId())
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		targetUser.update(
				updateCommand.name(),
				updateCommand.nickname()
		);

		// TODO: 회원정보 수정 완료 -> 다른 도메인에 저장된 회원 정보를 업데이트하기 위한 이벤트 발행

		return targetUser;
	}

	public User deleteUser(UUID userId) {
		User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));

		targetUser.delete(userId);

		return targetUser;
	}
}
