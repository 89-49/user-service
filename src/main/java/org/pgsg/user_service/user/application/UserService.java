package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.UserDetailInfo;
import org.pgsg.user_service.user.domain.entity.User;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public UserDetailInfo getUser(String username) {
		User user = userRepository.findByUsername(username)
				// TODO: 회원 도메인용 커스텀 예외 클래스로 대체
				.orElseThrow(() -> new IllegalArgumentException("해당 username을 사용하는 회원을 찾을 수 없습니다."));

		return UserDetailInfo.from(user);
	}
}
