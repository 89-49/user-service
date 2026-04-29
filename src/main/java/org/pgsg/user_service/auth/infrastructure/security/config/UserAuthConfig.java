package org.pgsg.user_service.auth.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.*;
import org.pgsg.user_service.auth.domain.JwtTokenProvider;
import org.pgsg.user_service.auth.infrastructure.security.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 로그인 인증에 필요한 추가적인 빈 등록
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Import(SecurityConfigImpl.class)
public class UserAuthConfig implements SecurityConfig {

    private final SecurityConfigImpl commonSecurityConfig;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup").permitAll() // 로그인, 회원가입 허용
                .requestMatchers("/internal/v1/users/**").permitAll()   // 추후 로그인용 회원정보 조회가 필요할 경우를 고려
            )
            .addFilterBefore(
                // JwtAuthenticationFilter에 관한 addFilterBefore를 LoginFilter에 관한 것보다 먼저 호출
                // 결과적으로는 JwtAuthenticationFilter(JWT 서명/유효시간 검증 및 요청헤더로 파싱)
                // -> LoginFilter(요청헤더 정보를 SecurityContext에 저장 + 토큰 재발급 시마다 비밀번호를 제외한 로그인한 회원의 정보를 갱신)
                // -> UsernamePasswordAuthenticationFilter(최초 로그인 시 아이디, 비밀번호 검증 수행)
                //     - 이미 로그인했다면 최초 로그인 시 SecurityContext에 저장된 아이디, 비밀번호를 사용해서 통과
                new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class
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
