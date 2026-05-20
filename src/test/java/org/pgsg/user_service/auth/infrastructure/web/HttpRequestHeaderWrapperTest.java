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
        mockRequest.addHeader("X-Normal-Header", "normal-value");
        
        // 2. 사칭 공격 시도 시뮬레이션 (클라이언트가 직접 보낸 X-User-* 헤더들)
        mockRequest.addHeader(JwtUtils.HEADER_USER_ID, "malicious-user-id");
        mockRequest.addHeader(JwtUtils.HEADER_USERNAME, "malicious-username");

        wrapper = new HttpRequestHeaderWrapper(mockRequest);
    }

    @Test
    @DisplayName("getHeaderNames - 인증 헤더와 추가된 헤더만 포함하고, 사칭 시도된 원본 헤더는 모두 제외한다")
    void getHeaderNames_Scenarios() {
        // given: 필터에서 일부 정보만 추가한 상황
        String realUserId = "real-user-123";
        wrapper.addHeader(JwtUtils.HEADER_USER_ID, realUserId);

        // when
        List<String> names = Collections.list(wrapper.getHeaderNames());

        // then
        // 1. 원본 일반 헤더들은 포함되어야 함
        assertThat(names).contains(HttpHeaders.AUTHORIZATION.toLowerCase(), "x-normal-header");
        
        // 2. 인증 후 추가된 헤더는 포함되어야 함
        assertThat(names).contains(JwtUtils.HEADER_USER_ID.toLowerCase());

        // 3. 원본에 있었으나 추가되지 않은 사칭 헤더(X-User-Username)는 제외되어야 함
        assertThat(names).doesNotContain(JwtUtils.HEADER_USERNAME.toLowerCase());

        // 4. 모든 헤더명은 소문자로 정규화되어 있어야 함 (Wrapper 구현 사양)
        assertThat(names).allMatch(name -> name.equals(name.toLowerCase()));
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
    @DisplayName("getHeaders - 추가된 헤더, 일반 원본 헤더, 금지된 원본 헤더에 대해 올바른 목록을 반환한다")
    void getHeaders_Scenarios() {
        // given
        String realUserId = "real-user-123";
        wrapper.addHeader(JwtUtils.HEADER_USER_ID, realUserId);

        // when & then 1: 추가된 헤더 조회 (대소문자 구분 없음)
        List<String> addedHeaders = Collections.list(wrapper.getHeaders(JwtUtils.HEADER_USER_ID));
        assertThat(addedHeaders).containsExactly(realUserId);
        
        List<String> addedHeadersLower = Collections.list(wrapper.getHeaders(JwtUtils.HEADER_USER_ID.toLowerCase()));
        assertThat(addedHeadersLower).containsExactly(realUserId);

        // when & then 2: 일반 원본 헤더 조회
        List<String> normalHeaders = Collections.list(wrapper.getHeaders(HttpHeaders.AUTHORIZATION));
        assertThat(normalHeaders).containsExactly("Bearer valid-access-token");

        // when & then 3: 금지된 원본 헤더(사칭 시도) 조회 -> 빈 목록이어야 함
        // 참고: 이미 위에서 JwtUtils.HEADER_USER_ID를 addHeader 했으므로, 
        // 사칭 시도 헤더를 테스트하기 위해 다른 금지된 헤더명을 사용
        List<String> forbiddenHeaders = Collections.list(wrapper.getHeaders(JwtUtils.HEADER_USERNAME));
        assertThat(forbiddenHeaders).isEmpty();
    }
}
