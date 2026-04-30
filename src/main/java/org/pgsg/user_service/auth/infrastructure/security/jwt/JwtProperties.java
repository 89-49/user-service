package org.pgsg.user_service.auth.infrastructure.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// TODO: 공통모듈의 config/security/jwt 패키지로 이전하되, 수동 빈 등록은 gateway-server, user-service 내부에서 수행
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
}