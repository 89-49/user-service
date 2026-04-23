package org.spartahub.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class LoginFilter extends OncePerRequestFilter {

    // 게이트웨이에서 전달하는 헤더 키 정의
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-User-Username"; // 로그인 ID로 사용
    private static final String HEADER_USER_NAME = "X-User-Name";  // 실명
    private static final String HEADER_ROLES = "X-User-Roles";    // 권한
    private static final String HEADER_ENABLED = "X-User-Enabled";

    private final HandlerExceptionResolver resolver;

    public LoginFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 매 요청마다 컨텍스트 초기화 후 인증 시도
            SecurityContextHolder.clearContext();
            doLogin(request);
        } catch (DisabledException e) {
            log.warn("[LoginFilter] 비활성화된 사용자 접근: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
            return;
        } catch (Exception e) {
            log.error("[LoginFilter] 인증 처리 중 오류 발생: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void doLogin(HttpServletRequest request) {
        // Trust boundary verification: Check if request comes from trusted gateway
        // In production, verify signed header/JWT or restrict to trusted proxy IPs
        String gatewaySignature = request.getHeader("X-Gateway-Signature");
        if (!StringUtils.hasText(gatewaySignature)) {
            log.warn("[LoginFilter] Missing gateway signature - rejecting request from untrusted source");
            throw new SecurityException("Request must come from trusted gateway");
        }
        // TODO: Implement actual signature verification (HMAC, JWT, or IP whitelist check)
        // For now, we fail closed if the signature header is missing

        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String usernameHeader = request.getHeader(HEADER_USERNAME);

        // 필수 정보(ID, Username)가 없으면 인증 처리를 하지 않고 넘어감 (Anonymous 상태 유지)
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(usernameHeader)) {
            return;
        }

        try {
            // 1. 헤더 값 추출 및 URL 디코딩 (한글 이름 등 깨짐 방지)
            UUID uuid = UUID.fromString(userIdHeader.trim());
            String username = usernameHeader.trim();
            String name = decodeHeader(request.getHeader(HEADER_USER_NAME));
            String roles = request.getHeader(HEADER_ROLES);
            String enabledStr = request.getHeader(HEADER_ENABLED);

            // 2. UserDetailsImpl 빌더 생성 (클래스 구조에 맞춤)
            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                    .uuid(uuid) // UUID 변환
                    .username(username)                 // 로그인 아이디
                    .password("")                       // 인증은 Gateway에서 완료됨
                    .userRole(roles)                    // roles(X) -> userRole(O) 매핑
                    .name(name)                         // 실명
                    .enabled("true".equalsIgnoreCase(enabledStr)) // boolean 변환
                    .build();

            // 3. 계정 활성화 여부 체크
            if (!userDetails.isEnabled()) {
                throw new DisabledException("승인 대기 중이거나 사용이 중지된 계정입니다.");
            }

            // 4. SecurityContext에 인증 객체 저장
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            log.warn("[LoginFilter] 유효하지 않은 UUID 형식: {}", userIdHeader);
        }
    }

    private String decodeHeader(String value) {
        if (!StringUtils.hasText(value)) return "";
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[LoginFilter] 헤더 디코딩 실패: {}", value);
            return value;
        }
    }
}