package org.pgsg.user_service.auth.infrastructure.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.jwt.JwtUtils;
import org.pgsg.config.security.token.TokenProvider;
import org.pgsg.user_service.auth.domain.UserAuthenticator;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private FilterChain filterChain;

    private final String accessToken = "valid.access.token";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider, userAuthenticator);
    }

    @Test
    @DisplayName("성공: 유효한 토큰인 경우 Claims를 추출하여 헤더에 담는다")
    void doFilterInternal_Success() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
        doNothing().when(userAuthenticator).checkBlacklist(accessToken);

        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add(JwtUtils.CLAIM_TOKEN_TYPE, "access")
                .add(JwtUtils.CLAIM_USERNAME, "testuser")
                .add(JwtUtils.CLAIM_USER_ROLE, "ROLE_USER")
                .add(JwtUtils.CLAIM_NAME, "홍길동")
                .add(JwtUtils.CLAIM_NICKNAME, "길동이")
                .add(JwtUtils.CLAIM_ENABLED, true)
                .build();

        when(tokenProvider.parseClaims(accessToken)).thenReturn(claims);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isEqualTo(userId.toString());
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USERNAME)).isEqualTo("testuser");
        // 한글 이름은 인코딩되어 있어야 함
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_NAME)).isNotEqualTo("홍길동");
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_ENABLED)).isEqualTo("true");
    }

    @Test
    @DisplayName("실패: 블랙리스트 토큰인 경우 헤더 없이 다음 필터로 진행한다")
    void doFilterInternal_Blacklisted() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
        doThrow(new UserServiceException(UserErrorCode.UNAUTHORIZED))
                .when(userAuthenticator).checkBlacklist(accessToken);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isNull();
    }

    @Test
    @DisplayName("실패: 토큰 타입이 ACCESS가 아닌(REFRESH 등) 경우, 토큰 내 정보가 있더라도 헤더에 담지 않고 사칭 헤더도 차단한다")
    void doFilterInternal_InvalidTokenType() throws Exception {
        // given: 유효한 서명이지만 타입이 REFRESH인 토큰 + 사칭 시도 헤더
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.addHeader(JwtUtils.HEADER_USER_ID, "malicious-user-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken(accessToken)).thenReturn(true);

        // 토큰 내부에 사용자 ID가 들어있지만 타입이 'refresh'인 상황 시뮬레이션
        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add(JwtUtils.CLAIM_TOKEN_TYPE, "refresh")
                .build();
        when(tokenProvider.parseClaims(accessToken)).thenReturn(claims);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then: 다음 필터로 전달된 요청 확인
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        // 1. 토큰 내에 userId가 있었음에도 'access' 타입이 아니므로 헤더에 등록되지 않아야 함
        // 2. 원본 요청에 있던 사칭 헤더(malicious-user-id)도 차단되어야 함
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isNull();
    }

    @Test
    @DisplayName("실패: 토큰이 유효하지 않은(잘못된 서명 등) 경우, 사칭 헤더를 차단하고 다음 필터로 진행한다")
    void doFilterInternal_InvalidToken() throws Exception {
        // given: 유효하지 않은 토큰 + 사칭 헤더
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        request.addHeader(JwtUtils.HEADER_USER_ID, "malicious-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // validateToken이 false를 반환하는 상황
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then: 다음 필터로 위임될 때 사칭 방지용 Wrapper로 감싸졌는지 확인
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        // 유효하지 않은 토큰이므로 인증 정보가 없어야 하고, 사칭 헤더도 null이어야 함
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isNull();
    }

    @Test
    @DisplayName("실패: 토큰이 없는 경우에도 사칭 방지용 래퍼를 씌워 다음 필터로 진행한다")
    void doFilterInternal_NoToken() throws Exception {
        // given: Authorization 헤더가 없고 사칭 시도용 헤더만 있는 요청
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtils.HEADER_USER_ID, "malicious-user-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when: 필터 수행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then: 다음 필터로 위임될 때 사칭 방지용 Wrapper로 감싸졌는지 확인
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        // 1. 사칭 헤더(X-User-Id)가 null로 차단되어야 함
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isNull();
        // 2. 토큰이 없으므로 검증 로직을 호출하지 않아야 함
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("성공: 일부 Claim이 null인 경우 해당 헤더는 생성되지 않는다")
    void doFilterInternal_NullClaims_OmitHeaders() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
        doNothing().when(userAuthenticator).checkBlacklist(accessToken);

        // Name, Nickname, Enabled를 null로 설정
        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add(JwtUtils.CLAIM_TOKEN_TYPE, "access")
                .add(JwtUtils.CLAIM_USERNAME, "testuser")
                .add(JwtUtils.CLAIM_USER_ROLE, "ROLE_USER")
                .add(JwtUtils.CLAIM_NAME, null)
                .add(JwtUtils.CLAIM_NICKNAME, null)
                .add(JwtUtils.CLAIM_ENABLED, null)
                .build();

        when(tokenProvider.parseClaims(accessToken)).thenReturn(claims);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_ID)).isEqualTo(userId.toString());
        
        // null인 Claim들에 대해서는 헤더가 존재하지 않아야 함 (null 반환)
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_NAME)).isNull();
        assertThat(wrappedRequest.getHeader(JwtUtils.HEADER_USER_NICKNAME)).isNull();
    }
}
