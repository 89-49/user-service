package org.pgsg.user_service.user.infrastructure.config;

import org.pgsg.common.domain.OutboxRepository;
import org.pgsg.common.event.OutboxService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

// TODO: 공통모듈 outboxService 빈 설정 추가 시, 이 클래스 삭제
@Configuration
public class EventBeanConfig {

    /**
     * 공통 모듈의 EventConfig에서 필요로 하는 OutboxService 빈을 수동으로 등록합니다.
     * 공통 모듈(common) 내부에서 OutboxService가 빈으로 등록되어 있지 않아 발생하는 기동 오류를 해결합니다.
     */
    @Bean
    public OutboxService outboxService(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new OutboxService(outboxRepository, kafkaTemplate);
    }
}
