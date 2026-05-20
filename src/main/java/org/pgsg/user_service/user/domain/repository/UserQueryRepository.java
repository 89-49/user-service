package org.pgsg.user_service.user.domain.repository;

import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryRepository {

	Optional<User> findById(UUID userId);

	// 로그인 진행 시 사용할 회원 조회 메서드
	Optional<User> findByUsername(String username);

	Page<User> findAll(SearchUserQuery searchQuery, Pageable pageable);
}
