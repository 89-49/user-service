package org.pgsg.user_service.user.infrastructure.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;
import static org.pgsg.user_service.user.domain.model.QUser.user;

public class UserQueryCondition {

	public static BooleanBuilder createSearchCondition(SearchUserQuery condition) {
		BooleanBuilder booleanBuilder = new BooleanBuilder();

		if (condition == null) {
			return booleanBuilder;
		}

		return booleanBuilder
				.and(keywordContains(condition.keyword()))
				.and(userRoleEq(condition.userRole()))
				.and(nicknameContains(condition.nickname()))
				.and(nameContains(condition.name()))
				.and(usernameContains(condition.username()));
	}

	private static BooleanExpression keywordContains(String keyword) {
		return StringUtils.hasText(keyword) ?
				user.nickname.containsIgnoreCase(keyword)
						.or(user.name.containsIgnoreCase(keyword))
						.or(user.username.containsIgnoreCase(keyword)) : null;
	}

	private static BooleanExpression userRoleEq(UserRole userRole) {
		return userRole != null ? user.userRole.eq(userRole) : null;
	}

	private static BooleanExpression nicknameContains(String nickname) {
		return StringUtils.hasText(nickname) ? user.nickname.containsIgnoreCase(nickname) : null;
	}

	private static BooleanExpression nameContains(String name) {
		return StringUtils.hasText(name) ? user.nickname.containsIgnoreCase(name) : null;
	}

	private static BooleanExpression usernameContains(String username) {
		return StringUtils.hasText(username) ? user.nickname.containsIgnoreCase(username) : null;
	}


	public static OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
		return sort.stream()
				.map(orderSort -> {
					Order direction = orderSort.isAscending() ? ASC : DESC;

					return switch (orderSort.getProperty()) {
						case "createdAt" -> new OrderSpecifier<>(direction, user.createdAt);
						case "username" -> new OrderSpecifier<>(direction, user.username);
						case "name" -> new OrderSpecifier<>(direction, user.name);
						case "nickname" -> new OrderSpecifier<>(direction, user.nickname);
						case "userRole" -> new OrderSpecifier<>(direction, user.userRole);
						default -> new OrderSpecifier<>(DESC, user.createdAt);
					};
				})
				.toArray(OrderSpecifier[]::new);
	}
}
