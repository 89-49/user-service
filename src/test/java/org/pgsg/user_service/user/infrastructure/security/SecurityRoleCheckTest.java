package org.pgsg.user_service.user.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityRoleCheckTest {

    private SecurityRoleCheck securityRoleCheck;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        securityRoleCheck = new SecurityRoleCheck();
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    @DisplayName("hasRole(UserRole) - 사용자가 해당 권한을 가지고 있으면 true를 반환해야 한다")
    void hasRole_returnsTrueWhenUserHasRole() {
        // given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserRole()).thenReturn("USER");
        mockedSecurityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(userDetails));

        // when
        boolean result = securityRoleCheck.hasRole(UserRole.USER);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasRole(UserRole) - 사용자가 해당 권한을 가지고 있지 않으면 false를 반환해야 한다")
    void hasRole_returnsFalseWhenUserDoesNotHaveRole() {
        // given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserRole()).thenReturn("USER");
        mockedSecurityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(userDetails));

        // when
        boolean result = securityRoleCheck.hasRole(UserRole.MANAGER);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasRole - MASTER 권한을 가진 사용자는 모든 권한에 대해 true를 반환해야 한다")
    void hasRole_returnsTrueForMasterUser() {
        // given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserRole()).thenReturn("MASTER");
        mockedSecurityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(userDetails));

        // when
        boolean result = securityRoleCheck.hasRole(UserRole.USER);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasRole - 로그인하지 않은 사용자는 false를 반환해야 한다")
    void hasRole_returnsFalseWhenNotLoggedIn() {
        // given
        mockedSecurityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

        // when
        boolean result = securityRoleCheck.hasRole(UserRole.USER);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("checkUserAdmin - 대상 유저 역할이 MANAGER나 MASTER면 true를 반환해야 한다")
    void checkUserAdmin_returnsTrueForAdminRoles() {
        assertThat(securityRoleCheck.checkUserAdmin(UserRole.MANAGER)).isTrue();
        assertThat(securityRoleCheck.checkUserAdmin(UserRole.MASTER)).isTrue();
        assertThat(securityRoleCheck.checkUserAdmin(UserRole.USER)).isFalse();
    }

    @Test
    @DisplayName("checkUserSelf - 현재 로그인한 유저 ID와 요청 ID가 일치하면 true를 반환해야 한다")
    void checkUserSelf_returnsTrueWhenIdMatches() {
        // given
        UUID userId = UUID.randomUUID();
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));

        // when
        boolean result = securityRoleCheck.checkUserSelf(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkUserSelf - 현재 로그인한 유저 ID와 요청 ID가 일치하지 않으면 false를 반환해야 한다")
    void checkUserSelf_returnsFalseWhenIdDoesNotMatch() {
        // given
        UUID userId = UUID.randomUUID();
        UUID differentId = UUID.randomUUID();
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(userId));

        // when
        boolean result = securityRoleCheck.checkUserSelf(differentId);

        // then
        assertThat(result).isFalse();
    }
}
