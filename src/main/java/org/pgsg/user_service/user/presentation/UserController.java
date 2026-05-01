package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.presentation.dto.response.UserDetailResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
