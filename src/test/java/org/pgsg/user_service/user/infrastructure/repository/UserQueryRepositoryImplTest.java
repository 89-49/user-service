package org.pgsg.user_service.user.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.user_service.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.pgsg.user_service.user.domain.model.QUser.user;

@ExtendWith(MockitoExtension.class)
class UserQueryRepositoryImplTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private UserQueryRepositoryImpl userQueryRepository;

    @Test
    @DisplayName("findById - 사용자가 존재하면 Optional.of(User)를 반환해야 한다")
    @SuppressWarnings("unchecked")
    void findById_returnsUser() {
        // given
        UUID userId = UUID.randomUUID();
        User mockUser = mock(User.class);
        JPAQuery<User> query = mock(JPAQuery.class);

        given(queryFactory.selectFrom(user)).willReturn(query);
        given(query.where(any(), any())).willReturn(query);
        given(query.fetchOne()).willReturn(mockUser);

        // when
        Optional<User> result = userQueryRepository.findById(userId);

        // then
        assertThat(result).isPresent().contains(mockUser);
    }

    @Test
    @DisplayName("findByUsername - 사용자가 존재하면 Optional.of(User)를 반환해야 한다")
    @SuppressWarnings("unchecked")
    void findByUsername_returnsUser() {
        // given
        String username = "testuser";
        User mockUser = mock(User.class);
        JPAQuery<User> query = mock(JPAQuery.class);

        given(queryFactory.selectFrom(user)).willReturn(query);
        given(query.where(any(), any())).willReturn(query);
        given(query.fetchOne()).willReturn(mockUser);

        // when
        Optional<User> result = userQueryRepository.findByUsername(username);

        // then
        assertThat(result).isPresent().contains(mockUser);
    }
}
