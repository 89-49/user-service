package org.pgsg.user_service.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.user_service.user.application.dto.info.ChatTimeRangeInfo;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.pgsg.user_service.user.application.dto.info.UserDetailInfo;
import org.pgsg.user_service.user.application.dto.query.SearchChatTimeQuery;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.application.dto.result.UserSearchResult;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.service.RoleCheck;
import org.springframework.data.domain.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserQueryFacadeTest {

    @Mock
    private UserQueryService userQueryService;
    @Mock
    private RoleCheck roleCheck;

    @InjectMocks
    private UserQueryFacade userQueryFacade;

    @Test
    @DisplayName("getUserWithAuthCheck()는 관리자도 아니고 본인도 아니면 예외를 던져야 한다")
    void getUserWithAuthCheck_throwsExceptionWhenUnauthorized() {
        // given
        UUID userId = UUID.randomUUID();
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);
        given(roleCheck.checkUserSelf(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userQueryFacade.getUserWithAuthCheck(userId))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("getUserWithAuthCheck()는 본인인 경우 사용자 정보를 반환해야 한다")
    void getUserWithAuthCheck_returnsUserWhenSelf() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "pw", UserRole.USER, "이름", "닉");
        
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);
        given(roleCheck.checkUserSelf(userId)).willReturn(true);
        given(userQueryService.getUser(userId)).willReturn(user);

        // when
        UserDetailInfo result = userQueryFacade.getUserWithAuthCheck(userId);

        // then
        assertThat(result.username()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUserWithAuthCheck()는 관리자인 경우 사용자 정보를 반환해야 한다")
    void getUserWithAuthCheck_returnsUserWhenAdmin() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "pw", UserRole.USER, "이름", "닉");

        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userQueryService.getUser(userId)).willReturn(user);

        // when
        UserDetailInfo result = userQueryFacade.getUserWithAuthCheck(userId);

        // then
        assertThat(result.username()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUserList()는 관리자 권한이 없으면 예외를 던져야 한다")
    void getUserList_throwsExceptionWhenNotAdmin() {
        // given
        SearchUserQuery query = new SearchUserQuery(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userQueryFacade.getUserList(query, pageable))
                .isInstanceOf(UserServiceException.class);
    }

    @Test
    @DisplayName("getUserList()는 관리자 권한이 있으면 (data-test.sql의 admin1 조건 시뮬레이션) 필터링된 목록을 반환해야 한다")
    void getUserList_returnsPagedUsersWhenAdmin_SimulateSearch() {
        // given
        SearchUserQuery query = new SearchUserQuery("admin1", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        
        // data-test.sql의 admin1 데이터에 해당하는 엔티티 시뮬레이션
        User user = User.create("admin1", "pw", UserRole.MASTER, "마스터1", "MASTER_1");
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userQueryService.getUserList(query, pageable)).willReturn(page);

        // when
        Page<UserSearchResult> result = userQueryFacade.getUserList(query, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("admin1");
        verify(userQueryService).getUserList(query, pageable);
    }

    @Test
    @DisplayName("getUserList()는 관리자 권한이 있으면 (data-test.sql의 전체 MASTER 조건 시뮬레이션) 전체 목록을 반환해야 한다")
    void getUserList_returnsAllMasterUsers_SimulateSearch() {
        // given
        SearchUserQuery query = new SearchUserQuery(null, UserRole.MASTER, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        
        // data-test.sql의 6개 MASTER 데이터 시뮬레이션
        List<User> users = List.of(
            User.create("admin1", "pw", UserRole.MASTER, "마스터1", "MASTER_1"),
            User.create("admin2", "pw", UserRole.MASTER, "마스터2", "MASTER_2"),
            User.create("admin3", "pw", UserRole.MASTER, "마스터3", "MASTER_3"),
            User.create("admin4", "pw", UserRole.MASTER, "마스터4", "MASTER_4"),
            User.create("admin5", "pw", UserRole.MASTER, "마스터5", "MASTER_5"),
            User.create("admin6", "pw", UserRole.MASTER, "마스터6", "MASTER_6")
        );
        Page<User> page = new PageImpl<>(users, pageable, 6);

        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userQueryService.getUserList(query, pageable)).willReturn(page);

        // when
        Page<UserSearchResult> result = userQueryFacade.getUserList(query, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getContent().get(0).userRole()).isEqualTo(UserRole.MASTER);
        verify(userQueryService).getUserList(query, pageable);
    }

    @Test
    @DisplayName("getUserList()는 검색 조건에 맞는 사용자가 없으면 빈 페이지를 반환해야 한다")
    void getUserList_returnsEmptyPageWhenNoMatches() {
        // given
        SearchUserQuery query = new SearchUserQuery("nonexistent", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(roleCheck.hasRole(List.of(UserRole.MANAGER, UserRole.MASTER))).willReturn(true);
        given(userQueryService.getUserList(query, pageable)).willReturn(emptyPage);

        // when
        Page<UserSearchResult> result = userQueryFacade.getUserList(query, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(userQueryService).getUserList(query, pageable);
    }

    @Test
    @DisplayName("getUser()는 사용자 정보를 반환해야 한다")
    void getUser_returnsUserDetailInfo() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "pw", UserRole.USER, "이름", "닉");
        given(userQueryService.getUser(userId)).willReturn(user);

        // when
        UserDetailInfo result = userQueryFacade.getUser(userId);

        // then
        assertThat(result.username()).isEqualTo("testuser");
        verify(userQueryService).getUser(userId);
    }

    @Test
    @DisplayName("getUserForAuth(UUID)는 인증용 사용자 정보를 반환해야 한다")
    void getUserForAuth_byId_returnsLoginUserDetailInfo() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "pw", UserRole.USER, "이름", "닉");
        given(userQueryService.getUser(userId)).willReturn(user);

        // when
        LoginUserDetailInfo result = userQueryFacade.getUserForAuth(userId);

        // then
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.password()).isEqualTo("pw");
        verify(userQueryService).getUser(userId);
    }

    @Test
    @DisplayName("getUserForAuth(String)는 인증용 사용자 정보를 반환해야 한다")
    void getUserForAuth_byUsername_returnsLoginUserDetailInfo() {
        // given
        String username = "testuser";
        User user = User.create(username, "pw", UserRole.USER, "이름", "닉");
        given(userQueryService.getUserForAuth(username)).willReturn(user);

        // when
        LoginUserDetailInfo result = userQueryFacade.getUserForAuth(username);

        // then
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.password()).isEqualTo("pw");
        verify(userQueryService).getUserForAuth(username);
    }

    @Test
    @DisplayName("getAvailableChatTime()은 채팅 가능 시간대 정보 목록을 반환해야 한다")
    void getAvailableChatTime_returnsChatTimeRangeInfoList() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDate monday = LocalDate.of(2024, 5, 13);
        LocalTime time = LocalTime.of(10, 0);
        SearchChatTimeQuery query = new SearchChatTimeQuery(monday, time);

        ChatTimeRange range = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        given(userQueryService.getAvailableChatTime(userId, query)).willReturn(List.of(range));

        // when
        List<ChatTimeRangeInfo> result = userQueryFacade.getAvailableChatTime(userId, query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.get(0).startTime()).isEqualTo(LocalTime.of(9, 0));
        verify(userQueryService).getAvailableChatTime(userId, query);
    }
}
