package org.pgsg.user_service.auth.infrastructure.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.user_service.auth.domain.TokenProvider;
import org.pgsg.user_service.auth.domain.model.TokenType;
import org.pgsg.user_service.auth.infrastructure.security.jwt.JwtUtils;
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

	private final TokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 1. 요청에 포함된 Authorization 헤더 검증
		String accessToken = resolveToken(request);

		if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
			try {
				// 2. 사용자 정보 추출: 토큰에서 사용자 정보 추출
				Claims claims = jwtTokenProvider.parseClaims(accessToken);

				// 3. 토큰 타입 검증 (액세스 토큰만 허용)
				String tokenType = claims.get(JwtUtils.CLAIM_TOKEN_TYPE, String.class);
				if (!TokenType.ACCESS.matches(tokenType)) {
					log.warn("[JwtFilter] 유효한 액세스 토큰이 아님 (token_type: {})", tokenType);
					filterChain.doFilter(new HttpRequestHeaderWrapper(request), response);
					return;
				}

				// 4. 토큰에서 추출한 정보를 요청 헤더에 저장
				HttpServletRequest requestWrapper = createWrapperWithHeaders(request, claims);
				// 5. 다음 필터로 위임
				log.info("[JwtFilter] 토큰 claims 파싱 완료 - 다음 필터로 위임");
				filterChain.doFilter(requestWrapper, response);
				return;
			} catch (JwtException | IllegalArgumentException e) {
				log.warn("[JwtFilter] 토큰 claims 추출 중 예상치 못한 오류 발생 - {}", e.getMessage());
			}
		}

		log.info("[JwtFilter] 유효한 토큰이 없음 - 다음 필터로 위임");
		// Authorization 헤더 없이 X-User-* 헤더만 보낸 경우 현재 요청 헤더에 포함된 X-User-* 헤더를 모두 비움
		filterChain.doFilter(new HttpRequestHeaderWrapper(request), response);
	}

	private String resolveToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		return JwtUtils.resolveToken(authorizationHeader);
	}

	private HttpServletRequest createWrapperWithHeaders(HttpServletRequest request, Claims claims) {
		HttpRequestHeaderWrapper requestWrapper = new HttpRequestHeaderWrapper(request);

		requestWrapper.addHeader(JwtUtils.HEADER_USER_ID, claims.getSubject());
		requestWrapper.addHeader(JwtUtils.HEADER_USERNAME, claims.get(JwtUtils.CLAIM_USERNAME, String.class));
		requestWrapper.addHeader(JwtUtils.HEADER_ROLES, claims.get(JwtUtils.CLAIM_USER_ROLE, String.class));

		// 한글 헤더는 URL 인코딩 처리 (LoginFilter에서 디코딩함)
		requestWrapper.addHeader(JwtUtils.HEADER_USER_NAME, encodeValue(claims.get(JwtUtils.CLAIM_NAME, String.class)));
		requestWrapper.addHeader(JwtUtils.HEADER_USER_NICKNAME, encodeValue(claims.get(JwtUtils.CLAIM_NICKNAME, String.class)));

		// Enabled 여부는 boolean값을 string 타입으로 변환한 값을 저장
		Boolean enabled = claims.get(JwtUtils.CLAIM_ENABLED, Boolean.class);
		requestWrapper.addHeader(JwtUtils.HEADER_ENABLED, enabled != null ? enabled.toString() : "false");

		return requestWrapper;
	}

	private String encodeValue(String value) {
		if (value == null) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
