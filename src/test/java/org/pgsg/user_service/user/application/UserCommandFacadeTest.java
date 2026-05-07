package org.pgsg.user_service.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.user_service.user.application.dto.command.CreateUserCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserAdminCommand;
import org.pgsg.user_service.user.application.dto.command.UpdateUserSelfCommand;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.result.UserDeleteResult;
import org.pgsg.user_service.user.application.dto.result.UserUpdateResult;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandFacadeTest {

    @Mock
    private UserCommandService userCommandService;
    @Mock
    private RoleCheck roleCheck;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCommandFacade userCommandFacade;

    @Test
    @DisplayName("createUser()는 관리자 역할을 부여할 때 MASTER 권한이 없으면 예외를 던져야 한다")
    void createUser_throwsExceptionWhenAssigningAdminWithoutMasterRole() {
        // given
        CreateUserCommand command = new CreateUserCommand("admin", "pw", UserRole.MANAGER, "이름", "닉", Collections.emptyList());
        given(roleCheck.checkUserAdmin(UserRole.MANAGER)).willReturn(true);
        given(roleCheck.hasRole(UserRole.MASTER)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userCommandFacade.createUser(command))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("createUser()는 권한 조건을 충족한 경우 UserDetailInfo를 반환해야 한다")
    void createUser_returnsUserDetailInfoWhenConditionsMet() {
        // given
        CreateUserCommand command = new CreateUserCommand("testuser", "pw", UserRole.USER, "홍길동", "길동이", Collections.emptyList());
        User user = User.create("testuser", "pw", UserRole.USER, "홍길동", "길동이");
        
        given(roleCheck.checkUserAdmin(UserRole.USER)).willReturn(false);
        given(userCommandService.createUser(command)).willReturn(user);

        // when
        UserDetailInfo result = userCommandFacade.createUser(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.name()).isEqualTo("홍길동");
        verify(userCommandService).createUser(command);
    }

    @Test
    @DisplayName("createUser()는 MASTER 권한이 관리자 역할을 부여하는 경우 UserDetailInfo를 반환해야 한다")
    void createUser_returnsUserDetailInfoWhenAssigningAdminWithMasterRole() {
        // given
        CreateUserCommand command = new CreateUserCommand("admin", "pw", UserRole.MANAGER, "관리자", "어드민", Collections.emptyList());
        User user = User.create("admin", "pw", UserRole.MANAGER, "관리자", "어드민");

        given(roleCheck.checkUserAdmin(UserRole.MANAGER)).willReturn(true);
        given(roleCheck.hasRole(UserRole.MASTER)).willReturn(true);
        given(userCommandService.createUser(command)).willReturn(user);

        // when
        UserDetailInfo result = userCommandFacade.createUser(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.userRole()).isEqualTo(UserRole.MANAGER);
        verify(userCommandService).createUser(command);
    }

    @Test
    @DisplayName("updateMyProfile()은 본인이 아니면 예외를 던져야 한다")
    void updateMyProfile_throwsExceptionWhenNotSelf() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserSelfCommand command = new UpdateUserSelfCommand(userId, "새이름", "새닉", "pw", Collections.emptyList());
        given(roleCheck.checkUserSelf(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userCommandFacade.updateMyProfile(command))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("updateMyProfile()은 본인인 경우 비밀번호를 인코딩하여 수정을 요청해야 한다")
    void updateMyProfile_encodesPasswordAndCallsService() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserSelfCommand command = new UpdateUserSelfCommand(userId, "새이름", "새닉", "rawPassword", Collections.emptyList());
        User user = User.create("user", "encodedPassword", UserRole.USER, "새이름", "새닉");
        ReflectionTestUtils.setField(user, "userId", userId);
        
        given(roleCheck.checkUserSelf(userId)).willReturn(true);
        given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");
        given(userCommandService.updateUserProfile(any())).willReturn(user);

        // when
        UserUpdateResult result = userCommandFacade.updateMyProfile(command);

        // then
        ArgumentCaptor<UpdateUserSelfCommand> captor = ArgumentCaptor.forClass(UpdateUserSelfCommand.class);
        verify(passwordEncoder).encode("rawPassword");
        verify(userCommandService).updateUserProfile(captor.capture());
        
        UpdateUserSelfCommand capturedCommand = captor.getValue();
        assertThat(capturedCommand.password()).isEqualTo("encodedPassword");
        assertThat(capturedCommand.name()).isEqualTo("새이름");
        assertThat(capturedCommand.nickname()).isEqualTo("새닉");
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("updateMyProfile()은 비밀번호가 null이면 인코딩을 생략하고 서비스에 null을 전달하여 기존 비밀번호를 유지해야 한다")
    void updateMyProfile_maintainsExistingPasswordWhenPasswordIsNull() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserSelfCommand command = new UpdateUserSelfCommand(userId, "새이름", "새닉", null, Collections.emptyList());
        User user = User.create("user", "oldPassword", UserRole.USER, "새이름", "새닉");
        ReflectionTestUtils.setField(user, "userId", userId);

        given(roleCheck.checkUserSelf(userId)).willReturn(true);
        given(userCommandService.updateUserProfile(any())).willReturn(user);

        // when
        userCommandFacade.updateMyProfile(command);

        // then
        verify(passwordEncoder, never()).encode(any());
        
        ArgumentCaptor<UpdateUserSelfCommand> captor = ArgumentCaptor.forClass(UpdateUserSelfCommand.class);
        verify(userCommandService).updateUserProfile(captor.capture());
        
        // 서비스로 전달되는 command의 password가 null임을 확인 (Service/Entity에서 기존 비밀번호를 유지하도록 함)
        assertThat(captor.getValue().password()).isNull();
    }

    @Test
    @DisplayName("updateMyProfile()은 비밀번호가 비어있으면 인코딩을 생략하고 서비스에 null을 전달하여 기존 비밀번호를 유지해야 한다")
    void updateMyProfile_maintainsExistingPasswordWhenPasswordIsBlank() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserSelfCommand command = new UpdateUserSelfCommand(userId, "새이름", "새닉", "  ", Collections.emptyList());
        User user = User.create("user", "oldPassword", UserRole.USER, "새이름", "새닉");
        ReflectionTestUtils.setField(user, "userId", userId);

        given(roleCheck.checkUserSelf(userId)).willReturn(true);
        given(userCommandService.updateUserProfile(any())).willReturn(user);

        // when
        userCommandFacade.updateMyProfile(command);

        // then
        verify(passwordEncoder, never()).encode(any());

        ArgumentCaptor<UpdateUserSelfCommand> captor = ArgumentCaptor.forClass(UpdateUserSelfCommand.class);
        verify(userCommandService).updateUserProfile(captor.capture());
        assertThat(captor.getValue().password()).isNull();
    }

    @Test
    @DisplayName("updateUserByAdmin()은 관리자 권한(MANAGER/MASTER)이 없으면 예외를 던져야 한다")
    void updateUserByAdmin_throwsExceptionWhenNotAdmin() {
        // given
        UpdateUserAdminCommand command = new UpdateUserAdminCommand(UUID.randomUUID(), "이름", "닉", UserRole.USER);
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userCommandFacade.updateUserByAdmin(command))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("updateUserByAdmin()은 관리자 권한이 있으면 수정을 허용해야 한다")
    void updateUserByAdmin_callsServiceWhenAdmin() {
        // given
        UpdateUserAdminCommand command = new UpdateUserAdminCommand(UUID.randomUUID(), "이름", "닉", UserRole.MANAGER);
        User user = User.create("user", "pw", UserRole.MANAGER, "이름", "닉");
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userCommandService.updateUserByAdmin(command)).willReturn(user);

        // when
        UserUpdateResult result = userCommandFacade.updateUserByAdmin(command);

        // then
        assertThat(result).isNotNull();
        verify(userCommandService).updateUserByAdmin(command);
    }

    @Test
    @DisplayName("deleteUser()는 관리자도 아니고 본인도 아니면 예외를 던져야 한다")
    void deleteUser_throwsExceptionWhenUnauthorized() {
        // given
        UUID targetUserId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);
        given(roleCheck.checkUserSelf(targetUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userCommandFacade.deleteUser(targetUserId, actorId))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("deleteUser()는 본인인 경우 삭제를 허용해야 한다")
    void deleteUser_callsServiceWhenSelf() {
        // given
        UUID targetUserId = UUID.randomUUID();
        UUID actorId = targetUserId;
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");
        
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);
        given(roleCheck.checkUserSelf(targetUserId)).willReturn(true);
        given(userCommandService.deleteUser(targetUserId, actorId)).willReturn(user);

        // when
        UserDeleteResult result = userCommandFacade.deleteUser(targetUserId, actorId);

        // then
        assertThat(result).isNotNull();
        verify(userCommandService).deleteUser(targetUserId, actorId);
    }

    @Test
    @DisplayName("deleteUser()는 관리자인 경우 타인 삭제를 허용해야 한다")
    void deleteUser_callsServiceWhenAdmin() {
        // given
        UUID targetUserId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = User.create("user", "pw", UserRole.USER, "이름", "닉");

        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userCommandService.deleteUser(targetUserId, actorId)).willReturn(user);

        // when
        UserDeleteResult result = userCommandFacade.deleteUser(targetUserId, actorId);

        // then
        assertThat(result).isNotNull();
        verify(userCommandService).deleteUser(targetUserId, actorId);
    }
}
