package org.pgsg.user_service.user.infrastructure.repository;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.user_service.user.application.dto.query.SearchUserQuery;
import org.pgsg.user_service.user.domain.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

class UserSearchConditionTest {

    @Test
    @DisplayName("createSearchCondition - 모든 조건이 null일 때 빈 BooleanBuilder를 반환해야 한다")
    void allNull() {
        SearchUserQuery query = new SearchUserQuery(null, null, null, null, null);
        BooleanBuilder builder = UserQueryCondition.createSearchCondition(query);
        assertThat(builder.hasValue()).isFalse();
    }

    @Test
    @DisplayName("createSearchCondition - 키워드 조건이 포함되어야 한다")
    void withKeyword() {
        SearchUserQuery query = new SearchUserQuery("test", null, null, null, null);
        BooleanBuilder builder = UserQueryCondition.createSearchCondition(query);
        String result = builder.toString().toLowerCase();
        assertThat(result).contains("nickname").contains("name").contains("username").contains("test");
    }

    @Test
    @DisplayName("createSearchCondition - 개별 필드 조건들이 각각 올바르게 포함되어야 한다")
    void individualFields() {
        assertThat(UserQueryCondition.createSearchCondition(new SearchUserQuery(null, null, "nick", null, null)).toString().toLowerCase())
                .contains("nickname").contains("nick");
        assertThat(UserQueryCondition.createSearchCondition(new SearchUserQuery(null, null, null, "realname", null)).toString().toLowerCase())
                .contains("name").contains("realname");
        assertThat(UserQueryCondition.createSearchCondition(new SearchUserQuery(null, null, null, null, "loginid")).toString().toLowerCase())
                .contains("username").contains("loginid");
    }

    @Test
    @DisplayName("createSearchCondition - 역할 조건이 정확히 일치해야 한다")
    void withRole() {
        SearchUserQuery query = new SearchUserQuery(null, UserRole.MANAGER, null, null, null);
        BooleanBuilder builder = UserQueryCondition.createSearchCondition(query);
        assertThat(builder.toString()).contains("user.userRole = MANAGER");
    }

    @Test
    @DisplayName("createSearchCondition - 빈 문자열이나 공백은 조건에서 제외되어야 한다")
    void ignoreEmptyStrings() {
        SearchUserQuery query = new SearchUserQuery("  ", null, "", " \n ", null);
        BooleanBuilder builder = UserQueryCondition.createSearchCondition(query);
        assertThat(builder.hasValue()).isFalse();
    }
}
