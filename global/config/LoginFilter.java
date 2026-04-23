package org.spartahub.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LoginFilter extends OncePerRequestFilter {

    // 게이트웨이에서 전달하는 헤더 키 정의
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-User-Username"; // 로그인 ID로 사용
    private static final String HEADER_USER_NAME = "X-User-Name";  // 실명
    private static final String HEADER_ROLES = "X-User-Roles";    // 권한
    private static final String HEADER_ENABLED = "X-User-Enabled";

    // Gateway signature verification headers
    private static final String HEADER_GATEWAY_SIGNATURE = "X-Gateway-Signature";
    private static final String HEADER_GATEWAY_TIMESTAMP = "X-Gateway-Timestamp";
    private static final String HEADER_GATEWAY_NONCE = "X-Gateway-Nonce";

    // Signature verification settings
    private static final long TIMESTAMP_TOLERANCE_MS = TimeUnit.MINUTES.toMillis(5); // 5 minutes
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // Nonce store to prevent replay attacks (entries expire after tolerance window)
    private final Map<String, Long> nonceStore = new ConcurrentHashMap<>();

    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Value("${gateway.signature.secret:change-this-secret-in-production}")
    private String gatewaySecret;

    public LoginFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 매 요청마다 컨텍스트 초기화 후 인증 시도
            SecurityContextHolder.clearContext();
            doLogin(request);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            // For authentication errors (DisabledException, etc.), delegate to AuthenticationEntryPoint
            // This ensures proper handling by Spring Security's CustomAuthenticationEntryPoint
            log.warn("[LoginFilter] 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        }
        // Note: Other exceptions (IllegalArgumentException, etc.) are caught in doLogin and logged,
        // but don't prevent the filter chain from continuing (request proceeds as anonymous)
    }

    private void doLogin(HttpServletRequest request) {
        // Trust boundary verification: Verify request comes from trusted gateway
        verifyGatewaySignature(request);

        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String usernameHeader = request.getHeader(HEADER_USERNAME);

        // 필수 정보(ID, Username)가 없으면 인증 처리를 하지 않고 넘어감 (Anonymous 상태 유지)
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(usernameHeader)) {
            return;
        }

        try {
            // 1. 헤더 값 추출 및 URL 디코딩 (한글 이름 등 깨짐 방지)
            UUID uuid = UUID.fromString(userIdHeader.trim());
            String username = usernameHeader.trim();
            String name = decodeHeader(request.getHeader(HEADER_USER_NAME));
            String roles = request.getHeader(HEADER_ROLES);
            String enabledStr = request.getHeader(HEADER_ENABLED);

            // 2. UserDetailsImpl 빌더 생성 (클래스 구조에 맞춤)
            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                    .uuid(uuid) // UUID 변환
                    .username(username)                 // 로그인 아이디
                    .password("")                       // 인증은 Gateway에서 완료됨
                    .userRole(roles)                    // roles(X) -> userRole(O) 매핑
                    .name(name)                         // 실명
                    .enabled("true".equalsIgnoreCase(enabledStr)) // boolean 변환
                    .build();

            // 3. 계정 활성화 여부 체크
            if (!userDetails.isEnabled()) {
                throw new DisabledException("승인 대기 중이거나 사용이 중지된 계정입니다.");
            }

            // 4. SecurityContext에 인증 객체 저장
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            log.warn("[LoginFilter] 유효하지 않은 UUID 형식: {}", userIdHeader);
        }
    }

    private String decodeHeader(String value) {
        if (!StringUtils.hasText(value)) return "";
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[LoginFilter] 헤더 디코딩 실패: {}", value);
            return value;
        }
    }

    /**
     * Verifies the gateway signature using HMAC-SHA256
     *
     * Validates:
     * 1. Presence of signature, timestamp, and nonce headers
     * 2. Timestamp is within allowed window (prevents replay attacks)
     * 3. Nonce has not been used before (prevents replay attacks)
     * 4. HMAC-SHA256 signature matches expected value
     *
     * @param request the HTTP request
     * @throws SecurityException if any validation fails
     */
    private void verifyGatewaySignature(HttpServletRequest request) {
        // 1. Extract required headers
        String providedSignature = request.getHeader(HEADER_GATEWAY_SIGNATURE);
        String timestampStr = request.getHeader(HEADER_GATEWAY_TIMESTAMP);
        String nonce = request.getHeader(HEADER_GATEWAY_NONCE);

        if (!StringUtils.hasText(providedSignature)) {
            log.warn("[LoginFilter] Missing gateway signature - rejecting request from untrusted source");
            throw new SecurityException("Request must come from trusted gateway");
        }

        if (!StringUtils.hasText(timestampStr)) {
            log.warn("[LoginFilter] Missing gateway timestamp - rejecting request");
            throw new SecurityException("Gateway timestamp is required");
        }

        if (!StringUtils.hasText(nonce)) {
            log.warn("[LoginFilter] Missing gateway nonce - rejecting request");
            throw new SecurityException("Gateway nonce is required");
        }

        // 2. Validate timestamp is within allowed window
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            log.warn("[LoginFilter] Invalid timestamp format: {}", timestampStr);
            throw new SecurityException("Invalid gateway timestamp");
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);

        if (timeDiff > TIMESTAMP_TOLERANCE_MS) {
            log.warn("[LoginFilter] Timestamp outside allowed window. Diff: {}ms, Max: {}ms",
                    timeDiff, TIMESTAMP_TOLERANCE_MS);
            throw new SecurityException("Gateway timestamp expired or invalid");
        }

        // 3. Check nonce hasn't been used (prevent replay attacks)
        // Clean up old nonces first
        cleanupExpiredNonces(currentTime);

        if (nonceStore.putIfAbsent(nonce, currentTime) != null) {
            log.warn("[LoginFilter] Nonce reuse detected - possible replay attack. Nonce: {}", nonce);
            throw new SecurityException("Gateway nonce has already been used");
        }

        // 4. Compute expected HMAC-SHA256 signature
        String canonicalRequest = buildCanonicalRequest(request, timestampStr, nonce);
        String expectedSignature = computeHmacSha256(canonicalRequest, gatewaySecret);

        // 5. Constant-time comparison to prevent timing attacks
        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            log.warn("[LoginFilter] Gateway signature mismatch - rejecting request");
            throw new SecurityException("Invalid gateway signature");
        }

        log.debug("[LoginFilter] Gateway signature verified successfully");
    }

    /**
     * Builds a canonical request string for signature computation
     * Format: METHOD\nPATH\nTIMESTAMP\nNONCE
     */
    private String buildCanonicalRequest(HttpServletRequest request, String timestamp, String nonce) {
        return request.getMethod() + "\n" +
               request.getRequestURI() + "\n" +
               timestamp + "\n" +
               nonce;
    }

    /**
     * Computes HMAC-SHA256 signature
     */
    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("[LoginFilter] Failed to compute HMAC signature", e);
            throw new SecurityException("Signature computation failed");
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(aBytes, bBytes);
    }

    /**
     * Removes expired nonces from the store to prevent memory leaks
     */
    private void cleanupExpiredNonces(long currentTime) {
        nonceStore.entrySet().removeIf(entry ->
            (currentTime - entry.getValue()) > TIMESTAMP_TOLERANCE_MS
        );
    }
}