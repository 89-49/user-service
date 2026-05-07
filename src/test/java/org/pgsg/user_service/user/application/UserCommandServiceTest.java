package org.pgsg.user_service.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserAdminCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserSelfCommand;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("createUser()는 중복된 username이 없으면 사용자를 저장해야 한다")
    void createUser_savesUserWhenNotDuplicate() {
        // given
        CreateUserCommand command = new CreateUserCommand("testuser", "password", UserRole.USER, "홍길동", "길동이", Collections.emptyList());
        given(userRepository.existsByUsername(command.username())).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userCommandService.createUser(command);

        // then
        assertThat(result.getUsername()).isEqualTo(command.username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser()는 중복된 username이 있으면 DUPLICATE_USER 예외를 던져야 한다")
    void createUser_throwsExceptionWhenDuplicate() {
        // given
        CreateUserCommand command = new CreateUserCommand("testuser", "password", UserRole.USER, "홍길동", "길동이", Collections.emptyList());
        given(userRepository.existsByUsername(command.username())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCommandService.createUser(command))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.DUPLICATE_USER);
    }

    @Test
    @DisplayName("updateUserProfile()은 사용자가 존재하면 프로필을 수정해야 한다")
    void updateUserProfile_updatesProfileWhenUserExists() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserSelfCommand command = new UpdateUserSelfCommand(userId, "새이름", "새닉네임", "새비밀번호", Collections.emptyList());
        User user = User.create("testuser", "oldPassword", UserRole.USER, "이름", "닉네임");
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User result = userCommandService.updateUserProfile(command);

        // then
        assertThat(result.getName()).isEqualTo("새이름");
        assertThat(result.getNickname()).isEqualTo("새닉네임");
        assertThat(result.getPassword()).isEqualTo("새비밀번호");
    }

    @Test
    @DisplayName("updateUserByAdmin()은 사용자가 존재하면 역할을 포함하여 수정해야 한다")
    void updateUserByAdmin_updatesRoleAndProfile() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserAdminCommand command = new UpdateUserAdminCommand(userId, "관리자이름", "관리자닉네임", UserRole.MANAGER);
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User result = userCommandService.updateUserByAdmin(command);

        // then
        assertThat(result.getUserRole()).isEqualTo(UserRole.MANAGER);
        assertThat(result.getName()).isEqualTo("관리자이름");
    }

    @Test
    @DisplayName("deleteUser()는 사용자를 삭제(소프트 삭제)해야 한다")
    void deleteUser_deletesUser() {
        // given
        UUID targetId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");

        given(userRepository.findById(targetId)).willReturn(Optional.of(user));

        // when
        User result = userCommandService.deleteUser(targetId, actorId);

        // then
        assertThat(result.isEnabled()).isFalse();
    }
}
