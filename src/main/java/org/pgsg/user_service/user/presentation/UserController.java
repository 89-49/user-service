package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.application.UserQueryFacade;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.presentation.dto.request.UserSearchRequest;
import org.pgsg.user_service.user.presentation.dto.response.UserDetailResponse;
import org.pgsg.user_service.user.presentation.dto.response.UserPageResponse;
import org.pgsg.user_service.user.presentation.dto.response.UserSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserQueryFacade userQueryFacade;

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") UUID userId) {
		UserDetailInfo userDetailInfo = userQueryFacade.getUserWithAuthCheck(userId);

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping("/me")
	public UserDetailResponse getUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		UserDetailInfo userDetailInfo = userQueryFacade.getUserWithAuthCheck(userDetails.getUuid());

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping
	public UserPageResponse<UserSearchResponse> getUserList(
			@ModelAttribute UserSearchRequest searchRequest,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<UserSearchResult> userList = userQueryFacade.getUserList(searchRequest.toQuery(), pageable);

		return UserPageResponse.from(userList.map(UserSearchResponse::from));
	}
}
