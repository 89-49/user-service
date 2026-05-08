package org.pgsg.user_service.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.user_service.auth.application.service.TokenService;
import org.pgsg.user_service.auth.presentation.dto.request.UserVerifyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("POST /internal/v1/auth/verify - 성공: 블랙리스트에 없으면 true 반환")
    void verifyToken_Valid() throws Exception {
        // given
        UserVerifyRequest request = new UserVerifyRequest("valid.token");
        String requestJson = objectMapper.writeValueAsString(request);
        given(tokenService.isBlacklisted("valid.token")).willReturn(false);

        // when & then
        mockMvc.perform(post("/internal/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isVerifiedToken").value(true));
    }

    @Test
    @DisplayName("POST /internal/v1/auth/verify - 성공: 블랙리스트에 있으면 false 반환")
    void verifyToken_Blacklisted() throws Exception {
        // given
        UserVerifyRequest request = new UserVerifyRequest("blacklisted.token");
        String requestJson = objectMapper.writeValueAsString(request);
        given(tokenService.isBlacklisted("blacklisted.token")).willReturn(true);

        // when & then
        mockMvc.perform(post("/internal/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isVerifiedToken").value(false));
    }
}
