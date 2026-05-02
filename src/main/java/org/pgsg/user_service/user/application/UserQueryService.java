package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.repository.UserQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 트랜잭션 적용구간을 단축
@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserQueryRepository userRepository;

	@Transactional(readOnly = true)
	public User getUser(UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public User getUserForAuth(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UserServiceException(UserErrorCode.USER_NOT_FOUND, "username"));
	}

	@Transactional(readOnly = true)
	public Page<User> getUserList(SearchUserQuery searchQuery, Pageable pageable) {
		Page<User> userPages = userRepository.findAll(searchQuery, pageable);

		if (userPages.isEmpty()) {
			throw new UserServiceException(UserErrorCode.USER_NOT_FOUND, "userList");
		}

		return userPages;
	}
}
