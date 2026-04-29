package org.pgsg.user_service.auth.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

// TODO: 현재 임시로 구현한 내부 로직을 나중에 게이트웨이로 이전
public class HttpRequestHeaderWrapper extends HttpServletRequestWrapper {

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
		return headerMap.getOrDefault(name.toLowerCase(), super.getHeader(name));
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		// 추가된 헤더에 관한 목록 단위 값을 반환
		String value = headerMap.get(name.toLowerCase());
		if (value != null) {
			// 리스트의 길이가 1인 경우에도 호환
			return Collections.enumeration(Collections.singletonList(value));
		}
		// 원본 요청의 헤더명에 관한 목록 단위 값을 반환
		return super.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// 원본 요청에서의 헤더 중복 제거
		Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));

		// 새로 추가된 헤더들을 중복 없이 일괄 저장
		names.addAll(headerMap.keySet());

		// 원본 헤더+새로 추가한 헤더
		return Collections.enumeration(names);
	}
}
