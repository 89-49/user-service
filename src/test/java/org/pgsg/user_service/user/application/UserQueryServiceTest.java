package org.pgsg.user_service.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;
import org.pgsg.user_service.user.application.dto.query.SearchChatTimeQuery;
import org.pgsg.user_service.user.domain.model.ChatTimeRange;
import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.model.UserRole;
import org.pgsg.user_service.user.domain.repository.UserQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    @DisplayName("getUser()는 사용자가 존재하면 해당 사용자 엔티티를 반환해야 한다")
    void getUser_returnsUserWhenExists() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "password", UserRole.USER, "이름", "닉네임");
        given(userQueryRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User result = userQueryService.getUser(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUser()는 사용자가 없으면 UserServiceException을 던져야 한다")
    void getUser_throwsExceptionWhenNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userQueryRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQueryService.getUser(userId))
                .isInstanceOf(UserServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getUserForAuth()는 username으로 사용자를 조회해야 한다")
    void getUserForAuth_returnsUserByUsername() {
        // given
        String username = "testuser";
        User user = User.create(username, "password", UserRole.USER, "이름", "닉네임");
        given(userQueryRepository.findByUsername(username)).willReturn(Optional.of(user));

        // when
        User result = userQueryService.getUserForAuth(username);

        // then
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("getUserList()는 검색 조건에 맞는 사용자 페이지를 반환해야 한다")
    void getUserList_returnsPagedUsers() {
        // given
        SearchUserQuery query = new SearchUserQuery(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.create("user1", "pw", UserRole.USER, "이름", "닉");
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        
        given(userQueryRepository.findAll(query, pageable)).willReturn(page);

        // when
        Page<User> result = userQueryService.getUserList(query, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAvailableChatTime()은 사용자의 채팅 가능 시간대 목록을 반환해야 한다")
    void getAvailableChatTime_returnsChatTimeRanges() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("testuser", "pw", UserRole.USER, "이름", "닉");
        ChatTimeRange range = ChatTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        user.addChatTimeRangeList(List.of(range));

        LocalDate monday = LocalDate.of(2024, 5, 13);
        SearchChatTimeQuery query = new SearchChatTimeQuery(monday, LocalTime.of(10, 0));

        given(userQueryRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        List<ChatTimeRange> result = userQueryService.getAvailableChatTime(userId, query);

        // then
        assertThat(result).hasSize(1).containsExactly(range);
    }
}
