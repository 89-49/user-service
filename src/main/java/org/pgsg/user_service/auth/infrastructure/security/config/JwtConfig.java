package org.pgsg.user_service.auth.infrastructure.security.config;

import org.pgsg.config.security.jwt.JwtProperties;
import org.pgsg.config.security.jwt.JwtTokenProvider;
import org.pgsg.config.security.token.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JwtProperties.class)
public class JwtConfig {

	private final JwtProperties jwtProperties;

	public JwtConfig(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Bean
	public TokenProvider tokenProvider() {
		return new JwtTokenProvider(jwtProperties);
	}
}