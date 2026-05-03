package org.pgsg.user_service.user.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.application.UserCommandFacade;
import org.pgsg.user_service.user.application.UserQueryFacade;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.result.UserDeleteResult;
import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.application.dto.result.UserUpdateResult;
import org.pgsg.user_service.user.presentation.dto.request.UserAdminUpdateRequest;
import org.pgsg.user_service.user.presentation.dto.request.UserSearchRequest;
import org.pgsg.user_service.user.presentation.dto.request.UserSelfUpdateRequest;
import org.pgsg.user_service.user.presentation.dto.response.*;
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
	private final UserCommandFacade userCommandFacade;

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") UUID userId) {
		UserDetailInfo userDetailInfo = userQueryFacade.getUserWithAuthCheck(userId);

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping("/me")
	public UserDetailResponse getMyProfile(@RequestHeader("X-User-Id") UUID currentUserId) {
		UserDetailInfo userDetailInfo = userQueryFacade.getUserWithAuthCheck(currentUserId);

		return UserDetailResponse.from(userDetailInfo);
	}

	@GetMapping
	public UserPageResponse<UserSearchResponse> getUserList(
			@ModelAttribute UserSearchRequest searchRequest,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<UserSearchResult> userList = userQueryFacade.getUserList(searchRequest.toQuery(), pageable);

		return UserPageResponse.from(userList.map(UserSearchResponse::from));
	}

	@PatchMapping("/me")
	public UserUpdateResponse updateMyProfile(
			@RequestHeader("X-User-Id") UUID currentUserId,
			@Valid @RequestBody UserSelfUpdateRequest updateRequest) {
		UserUpdateResult updateResult = userCommandFacade
				.updateMyProfile(updateRequest.toCommand(currentUserId));

		return UserUpdateResponse.from(updateResult);
	}

	@PatchMapping("/{userId}")
	public UserUpdateResponse updateUserByAdmin(
			@PathVariable("userId") UUID userId,
			@Valid @RequestBody UserAdminUpdateRequest updateRequest) {
		UserUpdateResult updateResult
				= userCommandFacade.updateUserByAdmin(updateRequest.toCommand(userId));

		return UserUpdateResponse.from(updateResult);
	}

	@DeleteMapping("/{userId}")
	public UserDeleteResponse deleteUser(
			@RequestHeader("X-User-Id") UUID currentUserId,
			@PathVariable("userId") UUID userId) {
		UserDeleteResult deleteResult = userCommandFacade.deleteUser(userId, currentUserId);

		return UserDeleteResponse.from(deleteResult);
	}
}
