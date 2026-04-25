package org.pgsg.user_service.auth.application.dto.command;

import org.pgsg.user_service.user.application.dto.command.CreateChatTimeCommand;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.List;

/**
 * Auth 서비스 내에서 회원가입 로직 처리를 담당하는 Command 객체
 */
public record SignupUserCommand(
        String username,
        String password,
        UserRole userRole,
        String name,
        String nickname,
        List<CreateChatTimeCommand> chatTimeRanges
) {
}
