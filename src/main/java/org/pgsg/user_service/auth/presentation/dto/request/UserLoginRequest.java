package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;

public record UserLoginRequest(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
    public LoginUserCommand toCommand() {
        return new LoginUserCommand(username, password);
    }
}