package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.presentation.dto.request.UserSearchRequest;
import org.pgsg.user_service.user.presentation.dto.response.UserDetailResponse;
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

	private final UserService userService;

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") UUID userId) {
		UserDetailInfo userDetailInfo = userService.getUserForAdmin(userId);

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping("/me")
	public UserDetailResponse getUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		UserDetailInfo userDetailInfo = userService.getUser(userDetails.getUuid());

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping
	public Page<UserSearchResponse> getUserList(
			@ModelAttribute UserSearchRequest searchRequest,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<UserSearchResult> userList = userService.getUserList(searchRequest.toQuery(), pageable);

		return userList.map(UserSearchResponse::from);
	}
}
