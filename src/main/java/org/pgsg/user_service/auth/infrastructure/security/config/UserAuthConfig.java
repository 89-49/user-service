package org.pgsg.user_service.auth.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.*;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.auth.domain.TokenProvider;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import org.pgsg.user_service.auth.infrastructure.security.UserAuthenticatorImpl;
import org.pgsg.user_service.user.application.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 로그인 인증에 필요한 추가적인 빈 등록
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Import(SecurityConfigImpl.class)
public class UserAuthConfig implements SecurityConfig {

    private final SecurityConfigImpl commonSecurityConfig;
    private final TokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    @Lazy
    private UserAuthenticator userAuthenticator;

    @Bean
    public UserAuthenticator userAuthenticator(AuthenticationManager authenticationManager) {
        return new UserAuthenticatorImpl(authenticationManager, tokenService, userService);
    }

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup", "/api/v1/auth/reissue").permitAll()
                .requestMatchers("/internal/v1/users/**").permitAll()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, userAuthenticator), UsernamePasswordAuthenticationFilter.class
            );

        return commonSecurityConfig.filterChain(http);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
