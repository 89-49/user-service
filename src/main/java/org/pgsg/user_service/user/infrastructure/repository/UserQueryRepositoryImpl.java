package org.pgsg.user_service.user.infrastructure.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.repository.UserQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.pgsg.user_service.user.domain.model.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<User> findById(UUID userId) {
		return Optional.ofNullable(
				queryFactory.selectFrom(user)
						.where(user.userId.eq(userId), user.deletedAt.isNull())
						.fetchOne()
		);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return Optional.ofNullable(
				queryFactory.selectFrom(user)
						.where(user.username.eq(username), user.deletedAt.isNull())
						.fetchOne()
		);
	}

	@Override
	public Page<User> findAll(SearchUserQuery searchQuery, Pageable pageable) {
		BooleanBuilder booleanBuilder = UserQueryCondition.createSearchCondition(searchQuery);

		List<User> content = queryFactory
				.selectFrom(user)
				.where(user.deletedAt.isNull(), booleanBuilder)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.orderBy(UserQueryCondition.getOrderSpecifier(pageable.getSort()))
				.fetch();

		JPAQuery<Long> countQuery = queryFactory
				.select(user.count())
				.from(user)
				.where(user.deletedAt.isNull(), booleanBuilder);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}
}
