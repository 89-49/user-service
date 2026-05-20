package org.pgsg.user_service.auth.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserCommand {
    private String username;
    private String password;
}