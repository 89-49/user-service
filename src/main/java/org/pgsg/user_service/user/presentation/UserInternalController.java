package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.presentation.dto.response.LoginUserResponse;
import org.pgsg.user_service.user.presentation.dto.response.UserDetailResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class UserInternalController {

	private final UserService userService;

	@GetMapping
	public LoginUserResponse getUser(@RequestParam(value = "username") String username) {
		LoginUserDetailInfo userDetailInfo = userService.getUser(username);

		return LoginUserResponse.from(userDetailInfo);
	}

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") UUID userId) {
		UserDetailInfo userDetailInfo = userService.getUser(userId);

		return UserDetailResponse.from(userDetailInfo);
	}
}
