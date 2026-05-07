package org.pgsg.user_service.auth.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.config.security.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestHeaderWrapperTest {

	private HttpRequestHeaderWrapper wrapper;

    @BeforeEach
    void setUp() {
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // 1. 정상적인 인증 요청 상황 시뮬레이션
        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-access-token");
        
        // 2. 사칭 공격 시도 시뮬레이션 (클라이언트가 직접 보낸 X-User-* 헤더)
        mockRequest.addHeader(JwtUtils.HEADER_USER_ID, "malicious-user-id");

        wrapper = new HttpRequestHeaderWrapper(mockRequest);
    }

    @Test
    @DisplayName("getHeader - 원본 요청의 사칭 헤더(x-user-)는 무시하고 일반 헤더만 반환한다")
    void getHeader_ForbiddenHeader() {
        // when
        String forbiddenHeader = wrapper.getHeader(JwtUtils.HEADER_USER_ID);
        String normalHeader = wrapper.getHeader(HttpHeaders.AUTHORIZATION);

        // then
        assertThat(forbiddenHeader).isNull();
        assertThat(normalHeader).isEqualTo("Bearer valid-access-token");
    }

    @Test
    @DisplayName("인증 후: JwtFilter가 추가한 헤더는 정상적으로 조회되며, 원본 사칭 값은 덮어씌워진다")
    void addHeader_AuthenticatedUser() {
        // given
        String authenticatedUserId = "real-user-123";

        // when: JwtFilter가 토큰 검증 완료 후 사용자 정보를 Wrapper에 주입
        wrapper.addHeader(JwtUtils.HEADER_USER_ID, authenticatedUserId);

        // then: 추가된 인증 정보와 기존 인증 헤더가 공존함
        assertThat(wrapper.getHeader(JwtUtils.HEADER_USER_ID)).isEqualTo(authenticatedUserId);
        assertThat(wrapper.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer valid-access-token");
    }

    @Test
    @DisplayName("getHeaderNames - 인증 헤더와 추가된 헤더만 포함하고, 사칭 시도된 원본 헤더는 제외한다")
    void getHeaderNames_FiltersForbiddenAndIncludesAdded() {
        // given
        wrapper.addHeader(JwtUtils.HEADER_USER_ID, "real-user-123");

        // when
        List<String> names = Collections.list(wrapper.getHeaderNames());

        // then
        assertThat(names).contains(HttpHeaders.AUTHORIZATION.toLowerCase(), JwtUtils.HEADER_USER_ID.toLowerCase());
        // getHeaderNames 결과에는 소문자로 변환된 헤더명들이 포함됨
    }
}
