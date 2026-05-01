package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserVerifyRequest(
		@NotBlank
		String accessToken
) {
}
