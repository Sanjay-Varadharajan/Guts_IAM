package com.guts.Guts_IAM.unit_testing.controller;

import com.guts.Guts_IAM.auth.controller.AuthController;
import com.guts.Guts_IAM.common.exception.types.InvalidCredentialsException;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testLogin_success() throws Exception {

        JwtResponse mockResponse =
                new JwtResponse("token123", "refresh123", "USER");

        when(authService.login(any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMail": "test@gmail.com",
                                  "userPassword": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"))
                .andExpect(jsonPath("$.tokenType").value("USER"));

        verify(authService, times(1))
                .login(any(), any());
    }

    @Test
    void testLogin_invalidCredentials() throws Exception {

        when(authService.login(any(), any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMail": "wrong@gmail.com",
                                  "userPassword": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1))
                .login(any(), any());
    }


    @Test
    void testLogout_success() throws Exception {

        doNothing().when(authService)
                .logout(anyString(), any());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(authService, times(1))
                .logout(eq("refresh123"), any());
    }


    @Test
    void testLogout_missingToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}