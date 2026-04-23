package org.pgsg.user_service.user.presentation;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.UserDetailsInfo;
import org.pgsg.user_service.user.presentation.dto.GetUserDetailsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class UserInternalController {

	private final UserService userService;

	@GetMapping("/{username}")
	public GetUserDetailsResponse getUser(@PathVariable("username") String username) {
		UserDetailsInfo userDetailInfo = userService.getUser(username);

		return GetUserDetailsResponse.from(userDetailInfo);
	}
}
