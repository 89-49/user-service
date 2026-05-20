package org.pgsg.user_service.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.user.application.dto.info.ChatTimeRangeInfo;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.query.SearchChatTimeQuery;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryFacade {

	private final UserQueryService userQueryService;
	private final RoleCheck roleCheck;

	// /api/v1/users/** 기반 조회, 검색용
	public UserDetailInfo getUserWithAuthCheck(UUID userId) {
		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER)) && !roleCheck.checkUserSelf(userId)) {
			throw new UserServiceException(UserErrorCode.ADMIN_ACCESS_DENIED);
		}

		return getUser(userId);
	}

	public Page<UserSearchResult> getUserList(SearchUserQuery searchQuery, Pageable pageable) {
		if (!roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))) {
			throw new UserServiceException(UserErrorCode.ADMIN_ACCESS_DENIED);
		}

		Page<User> userList = userQueryService.getUserList(searchQuery, pageable);

		return userList.map(UserSearchResult::from);
	}


	// /internal/v1/** 기반 조회, 인증용
	public UserDetailInfo getUser(UUID userId) {
		User user = userQueryService.getUser(userId);

		return UserDetailInfo.from(user);
	}

	public LoginUserDetailInfo getUserForAuth(UUID userId) {
		// 토큰 재발급용
		User user = userQueryService.getUser(userId);

		return LoginUserDetailInfo.from(user);
	}

	public LoginUserDetailInfo getUserForAuth(String username) {
		User user = userQueryService.getUserForAuth(username);

		return LoginUserDetailInfo.from(user);
	}

	public List<ChatTimeRangeInfo> getAvailableChatTime(UUID userId, SearchChatTimeQuery chatTimeQuery) {
		List<ChatTimeRange> chatTimeRanges = userQueryService.getAvailableChatTime(userId, chatTimeQuery);

		return chatTimeRanges.stream()
				.map(ChatTimeRangeInfo::from)
				.toList();
	}
}
