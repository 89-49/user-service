package org.pgsg.user_service.auth.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.user_service.auth.application.dto.command.LoginUserCommand;

@Getter
@NoArgsConstructor
public class UserLoginRequest {
    private String username;
    private String password;

    public LoginUserCommand toCommand() {
        return new LoginUserCommand(username, password);
    }
}