package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;

@Getter
@NoArgsConstructor
public class UserLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public LoginUserCommand toCommand() {
        return new LoginUserCommand(username, password);
    }
}