package org.pgsg.user_service.auth.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;
import java.util.stream.Collectors;

// 게이트웨이의 JWT 인증 필터를 추가하기 전까지 임시 사용할 Wrapper
public class HttpRequestHeaderWrapper extends HttpServletRequestWrapper {

	// Authorization 헤더 없이 서버가 인증을 마쳤다는 증표로 사용되는 내부 헤더 적용 -> 서버 권한 사칭 시도로 해석
	private static final String FORBIDDEN_HEADER_PREFIX = "x-user-";

	private final Map<String, String> headerMap;

	public HttpRequestHeaderWrapper(HttpServletRequest request) {
		super(request);
		this.headerMap = new HashMap<>();
	}

	public void addHeader(String name, String value) {
		headerMap.put(name.toLowerCase(), value);
	}

	@Override
	public String getHeader(String name) {
		String lowerName = name.toLowerCase();
		if (headerMap.containsKey(lowerName)) {
			return headerMap.get(lowerName);
		}
		if (lowerName.startsWith(FORBIDDEN_HEADER_PREFIX)) {
			return null;
		}
		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		// 추가된 헤더에 관한 목록 단위 값을 반환
		String lowerName = name.toLowerCase();
		String value = headerMap.get(lowerName);

		if (headerMap.containsKey(lowerName)) {
			// 리스트의 길이가 1인 경우에도 호환
			return Collections.enumeration(Collections.singletonList(value));
		}
		if (lowerName.startsWith(FORBIDDEN_HEADER_PREFIX)) {
			return Collections.emptyEnumeration();
		}
		// 원본 요청의 헤더명에 관한 목록 단위 값을 반환
		return super.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// 원본 요청에서의 헤더 중복 제거
		Set<String> names = Collections.list(super.getHeaderNames()).stream()
				.map(String::toLowerCase)
				.filter(headerName ->
						!headerMap.containsKey(headerName) && 			// 직접 추가한 요청 헤더가 아니면서
						!headerName.startsWith(FORBIDDEN_HEADER_PREFIX)	//금지된 접두사로 시작하는 헤더가 아님
				)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// 새로 추가된 헤더들을 중복 없이 일괄 저장
		names.addAll(headerMap.keySet());

		// 원본 헤더+새로 추가한 헤더
		return Collections.enumeration(names);
	}
}
