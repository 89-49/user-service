package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.presentation.dto.request.ChatTimeRequest;

import java.util.List;

public record UserSignupRequest(

        @NotBlank(message = "[user.validation.user-info-id.required]")
        @Pattern(regexp = "^[a-zA-Z0-9]{8,12}$", message = "[user.validation.user-info-id.invalid-format]")
        String username,

        @NotBlank(message = "[user.validation.user-info-password.required]")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$", message = "[user.validation.user-info-password.invalid-pattern]")
        String password,

        @NotBlank(message = "[user.validation.user-info-role.required]")
        @Pattern(regexp = "^(?i)(USER|MANAGER|MASTER|ROLE_USER|ROLE_MANAGER|ROLE_MASTER|일반 사용자|관리자|총관리자)$", message = "[user.validation.user-info-role.invalid-pattern]")
        String userRole,

        @NotBlank(message = "[user.validation.user-info-name.required]")
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "[user.validation.user-info-name.invalid-pattern]")
        String name,

        @NotBlank(message = "[user.validation.user-info-nickname.required]")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,20}$", message = "[user.validation.user-info-nickname.invalid-pattern]")
        String nickname,

        @NotEmpty(message = "[user.validation.user-info-chat-time.range-required]")
        List<@NotNull @Valid ChatTimeRequest> chatTimeRanges
) {
    // Request DTO를 서비스 계층에서 사용할 Command 객체로 변환
    public SignupUserCommand toCommand() {
        return new SignupUserCommand(
                username,
                password,
                UserRole.find(userRole),
                name,
                nickname,
                chatTimeRanges.stream()
                        .map(ChatTimeRequest::toCommand)
                        .toList()
        );
    }
}
