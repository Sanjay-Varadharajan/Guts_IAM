package com.guts.Guts_IAM.unit_testing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.token.controller.RefreshTokenController;
import com.guts.Guts_IAM.token.refreshtoken.dto.TokenRefreshRequest;
import com.guts.Guts_IAM.token.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefreshTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RefreshTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testRefreshToken_Success() throws Exception {

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("dummy-refresh-token");

        JwtResponse responseMock = new JwtResponse("new-access-token", "refresh-token","Bearer");

        when(refreshTokenService.refreshAccessToken(
                eq("dummy-refresh-token"),
                any(HttpServletRequest.class)
        )).thenReturn(responseMock);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

}
