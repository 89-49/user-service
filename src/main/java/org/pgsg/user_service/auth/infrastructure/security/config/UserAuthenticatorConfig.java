package org.pgsg.user_service.auth.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.auth.infrastructure.security.UserAuthenticatorImpl;
import org.pgsg.user_service.user.application.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@RequiredArgsConstructor
public class UserAuthenticatorConfig {

	private final AuthenticationConfiguration authenticationConfiguration;
	private final TokenService tokenService;
	private final UserService userService;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public UserAuthenticator userAuthenticator() throws Exception {
		return new UserAuthenticatorImpl(
				authenticationConfiguration.getAuthenticationManager(),
				tokenService,
				userService
		);
	}
}