package org.pgsg.user_service.user.infrastructure.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class UserOrderSpecifierTest {

    @Test
    @DisplayName("getOrderSpecifier - 모든 지원하는 속성에 대해 올바른 OrderSpecifier를 생성해야 한다")
    void allSupportedProperties() {
        assertThat(getSpecifierTarget("createdAt")).isEqualTo("user.createdAt");
        assertThat(getSpecifierTarget("username")).isEqualTo("user.username");
        assertThat(getSpecifierTarget("name")).isEqualTo("user.name");
        assertThat(getSpecifierTarget("nickname")).isEqualTo("user.nickname");
        assertThat(getSpecifierTarget("userRole")).isEqualTo("user.userRole");
    }

    @Test
    @DisplayName("getOrderSpecifier - 여러 정렬 조건의 순서와 방향이 유지되어야 한다")
    void multiSort() {
        Sort sort = Sort.by(Sort.Order.asc("username"), Sort.Order.desc("name"));
        OrderSpecifier<?>[] specifiers = UserQueryCondition.getOrderSpecifier(sort);

        assertThat(specifiers).hasSize(2);
        assertThat(specifiers[0].getOrder()).isEqualTo(Order.ASC);
        assertThat(specifiers[1].getOrder()).isEqualTo(Order.DESC);
    }

    @Test
    @DisplayName("getOrderSpecifier - 지원하지 않는 속성이나 빈 Sort 요청 시의 동작을 검증한다")
    void fallbackToDefault() {
        assertThat(UserQueryCondition.getOrderSpecifier(Sort.unsorted())).isEmpty();
        
        OrderSpecifier<?>[] specifiers = UserQueryCondition.getOrderSpecifier(Sort.by("unknown"));
        assertThat(specifiers).hasSize(1);
        assertThat(specifiers[0].getTarget().toString()).isEqualTo("user.createdAt");
        assertThat(specifiers[0].getOrder()).isEqualTo(Order.DESC);
    }

    private String getSpecifierTarget(String property) {
        return UserQueryCondition.getOrderSpecifier(Sort.by(property))[0].getTarget().toString();
    }
}
