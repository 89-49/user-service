package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
