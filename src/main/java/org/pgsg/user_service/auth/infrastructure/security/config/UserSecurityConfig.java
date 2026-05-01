package org.pgsg.user_service.auth.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.*;
import org.pgsg.config.security.token.TokenProvider;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 로그인 인증에 필요한 추가적인 빈 등록
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Import({SecurityConfigImpl.class, JwtConfig.class, UserAuthenticatorConfig.class})
public class UserSecurityConfig implements SecurityConfig {

    private final TokenProvider jwtTokenProvider;
    private final LoginFilter loginFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http, UserAuthenticator userAuthenticator) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c
                        -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup", "/api/v1/auth/reissue").permitAll()
                        .requestMatchers("/internal/v1/users/**").permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                // JwtAuthenticationFilter가 먼저 실행되어 헤더를 세팅
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userAuthenticator),
                        UsernamePasswordAuthenticationFilter.class
                )
                // LoginFilter가 그 다음 실행되어 SecurityContext 저장
                .addFilterAfter(loginFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(authenticationEntryPoint);
                    c.accessDeniedHandler(accessDeniedHandler);
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}