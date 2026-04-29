package org.pgsg.user_service.auth.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;
import java.util.stream.Collectors;

// TODO: 게이트웨이로 JWT 인증 필터 분리 완료 시 deprecated 처리
// 게이트웨이의 JWT 인증 필터를 추가하기 전까지만 임시 사용할 Wrapper
public class HttpRequestHeaderWrapper extends HttpServletRequestWrapper {

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
			//
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
				.filter(headerName
						-> !headerMap.containsKey(headerName) && !headerName.startsWith(FORBIDDEN_HEADER_PREFIX))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// 새로 추가된 헤더들을 중복 없이 일괄 저장
		names.addAll(headerMap.keySet());

		// 원본 헤더+새로 추가한 헤더
		return Collections.enumeration(names);
	}
}
