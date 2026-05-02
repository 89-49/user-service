package org.pgsg.user_service.user.presentation.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class UserPageResponse<T> {

	private final List<T> content;
	private final int totalPages;
	private final long totalElements;
	private final int size;
	private final int number;

	private UserPageResponse(Page<T> page) {
		this.content = page.getContent();
		this.totalPages = page.getTotalPages();
		this.totalElements = page.getTotalElements();
		this.size = page.getSize();
		this.number = page.getNumber();
	}

	public static <T> UserPageResponse<T> from(Page<T> page) {
		return new UserPageResponse<>(page);
	}
}
