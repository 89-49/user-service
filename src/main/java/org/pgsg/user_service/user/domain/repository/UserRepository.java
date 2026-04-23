package org.pgsg.user_service.user.domain.repository;

import org.pgsg.user_service.user.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

// TODO: 공통모듈 배포 시 조회/검색 쿼리는 UserQueryRepository로 분리 후 QueryDsl을 사용하여 구현 예정
public interface UserRepository {

	Optional<User> findById(UUID userId);

	// 로그인 진행 시 사용할 회원 조회 메서드
	Optional<User> findByUsername(String username);
}
