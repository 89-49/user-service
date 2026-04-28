package org.pgsg.user_service.auth.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 로그인 인증에 필요한 추가적인 빈 등록
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Import(SecurityConfigImpl.class)
public class UserAuthConfig implements SecurityConfig {

    private final SecurityConfigImpl commonSecurityConfig;

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup").permitAll() // 로그인, 회원가입 허용
            .requestMatchers("/internal/v1/users/**").permitAll()
        );

        return commonSecurityConfig.filterChain(http);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 가장 권장되는 해시 알고리즘
    }
}
