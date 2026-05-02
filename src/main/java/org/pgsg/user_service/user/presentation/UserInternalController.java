package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.UserQueryFacade;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.presentation.dto.response.UserLoginResponse;
import org.pgsg.user_service.user.presentation.dto.response.UserDetailResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class UserInternalController {

	private final UserQueryFacade userQueryFacade;

	@GetMapping
	public UserLoginResponse getUser(@RequestParam(value = "username") String username) {
		LoginUserDetailInfo userDetailInfo = userQueryFacade.getUserForAuth(username);

		return UserLoginResponse.from(userDetailInfo);
	}

	@GetMapping("/{userId}")
	public UserDetailResponse getUser(@PathVariable("userId") UUID userId) {
		UserDetailInfo userDetailInfo = userQueryFacade.getUser(userId);

		return UserDetailResponse.from(userDetailInfo);
	}
}
