package org.pgsg.user_service.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.user_service.auth.application.dto.info.AuthInfo;
import org.pgsg.user_service.auth.application.dto.info.SignupInfo;
import org.pgsg.user_service.auth.application.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.pgsg.user_service.auth.presentation.dto.request.UserLoginRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserReissueRequest;
import org.pgsg.user_service.auth.presentation.dto.request.UserSignupRequest;
import org.pgsg.user_service.user.presentation.dto.request.ChatTimeRequest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();
    private final String accessToken = "access.token";
    private final String refreshToken = "refresh.token";

    @Test
    @DisplayName("POST /api/v1/auth/login - 성공")
    void login_Success() throws Exception {
        // given
        UserLoginRequest request = new UserLoginRequest("testuser", "password");
        AuthInfo authInfo = new AuthInfo(accessToken, refreshToken);
        given(authService.login(any())).willReturn(authInfo);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 성공")
    void signup_Success() throws Exception {
        // given
        ChatTimeRequest chatTimeRequest = new ChatTimeRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)
        );
        UserSignupRequest request = new UserSignupRequest(
                "testuser88", "password123!", "USER", "Tester", "testnick", List.of(chatTimeRequest)
        );

        SignupInfo signupInfo = new SignupInfo(userId, "testuser88", "testnick", LocalDateTime.now(), userId);
        given(authService.signup(any())).willReturn(signupInfo);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.username").value("testuser88"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reissue - 성공")
    void reissue_Success() throws Exception {
        // given
        UserReissueRequest request = new UserReissueRequest("old.access.token", "old.refresh.token");
        AuthInfo authInfo = new AuthInfo(accessToken, refreshToken);
        given(authService.reissue(any())).willReturn(authInfo);

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - 성공")
    void logout_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-User-Id", userId.toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(authService).logout(userId, "Bearer " + accessToken);
    }
}
