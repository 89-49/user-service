package org.pgsg.user_service.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;
import org.pgsg.user_service.auth.application.dto.command.ReissueUserCommand;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final TokenService tokenService;
	private final UserAuthenticator userAuthenticator;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AuthInfo login(LoginUserCommand command) {
		// 1. 사용자 인증 (비밀번호 확인)
		UserDetailsImpl userDetail = userAuthenticator.verify(command.getUsername(), command.getPassword());

		// 2. 토큰 발급 및 저장 위임
		return tokenService.issueTokenPair(userDetail);
	}

	@Transactional
	public void logout(UUID userId, String accessToken) {
		tokenService.deleteRefreshToken(userId);
		tokenService.addToBlacklist(userId, accessToken);
	}

	@Transactional
	public SignupInfo signup(SignupUserCommand command) {
		String encryptedPassword = passwordEncoder.encode(command.password());
		CreateUserCommand createUserCommand = new CreateUserCommand(
				command.username(),
				encryptedPassword,
				command.userRole(),
				command.name(),
				command.nickname(),
				command.chatTimeRanges()
		);

		UserDetailInfo userInfo = userService.createUser(createUserCommand);
		return SignupInfo.from(userInfo);
	}

	@Transactional
	public AuthInfo reissue(ReissueUserCommand command) {
		// 1. 인증기에게 토큰 검증 및 최신 정보 조회를 맡김
		UserDetailsImpl userDetails = userAuthenticator.verifyToken(command.accessToken(), command.refreshToken());

		// 2. 새로운 토큰 쌍 발급 및 저장 위임
		return tokenService.issueTokenPair(userDetails);
	}
}
