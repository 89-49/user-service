package org.pgsg.user_service.user.domain.repository;

import org.pgsg.user_service.user.domain.model.User;

// TODO: 공통모듈 배포 시 조회/검색 쿼리는 UserQueryRepository로 분리 후 QueryDsl을 사용하여 구현 예정
public interface UserRepository {

	User save(User user);

	boolean existsByUsername(String username);
}
