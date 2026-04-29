package org.pgsg.user_service.auth.infrastructure.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.auth.domain.TokenProvider;
import org.pgsg.user_service.auth.infrastructure.web.HttpRequestHeaderWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// TODO: 내부 로직은 gateway-server의 filter 패키지에서 활용(spring cloud gateway 사용 시, webflux로 전환 필요)
// TODO: gateway-server JWT 인증 필터 분리 완료 이후 deprecated 처리
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USERNAME = "X-User-Username";
	private static final String HEADER_ROLES = "X-User-Roles";
	private static final String HEADER_USER_NAME = "X-User-Name";
	private static final String HEADER_USER_NICKNAME = "X-User-Nickname";
	private static final String HEADER_ENABLED = "X-User-Enabled";

	private final TokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 1. 요청에 포함된 Authorization 헤더 검증
		String accessToken = resolveToken(request);
		if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
			log.info("[JwtFilter] 유효한 토큰이 없음 - 다음 필터로 위임");
			filterChain.doFilter(request, response);
			return;
		}

		// 2. 사용자 정보 추출: 토큰에서 사용자 정보 추출
		Claims claims = jwtTokenProvider.parseClaims(accessToken);

		// 3. 토큰에서 추출한 정보를 요청 헤더에 저장
		HttpServletRequest requestWrapper = createWrapperWithHeaders(request, claims);

		// 4. 다음 필터로 위임
		filterChain.doFilter(requestWrapper, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		// 비로그인 상태에서 요청을 보내거나 보낸 토큰이 accessToken에 해당되지 않는 경우
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			return null;
		}

		return authorizationHeader.substring(BEARER_PREFIX.length());
	}

	private HttpServletRequest createWrapperWithHeaders(HttpServletRequest request, Claims claims) {
		HttpRequestHeaderWrapper requestWrapper = new HttpRequestHeaderWrapper(request);

		requestWrapper.addHeader(HEADER_USER_ID, claims.getSubject());
		requestWrapper.addHeader(HEADER_USERNAME, claims.get("username", String.class));
		requestWrapper.addHeader(HEADER_ROLES, claims.get("role", String.class));

		// 한글 헤더는 URL 인코딩 처리 (LoginFilter에서 디코딩함)
		requestWrapper.addHeader(HEADER_USER_NAME, encodeValue(claims.get("name", String.class)));
		requestWrapper.addHeader(HEADER_USER_NICKNAME, encodeValue(claims.get("nickname", String.class)));

		// Enabled 여부는 boolean값을 string 타입으로 변환한 값을 저장
		requestWrapper.addHeader(HEADER_ENABLED, String.valueOf(claims.get("enabled", Boolean.class)));

		return requestWrapper;
	}

	private String encodeValue(String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
