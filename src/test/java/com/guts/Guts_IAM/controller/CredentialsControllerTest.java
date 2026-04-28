package com.guts.Guts_IAM.controller;

import com.guts.Guts_IAM.auth.controller.CredentialsController;
import com.guts.Guts_IAM.auth.service.PasswordAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CredentialsController.class)
@AutoConfigureMockMvc(addFilters = false)
class CredentialsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordAuthService passwordAuthService;

    @MockBean
    private com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testForgotPassword_success() throws Exception {

        doNothing().when(passwordAuthService).forgotPassword(any());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eMail": "test@gmail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true))
                .andExpect(jsonPath("$.responseMessage").value("OTP_SENT_SUCCESSFULLY"));

        verify(passwordAuthService, times(1))
                .forgotPassword(any());
    }

    @Test
    void testForgotPassword_invalidRequest() throws Exception {

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eMail": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(passwordAuthService, never())
                .forgotPassword(any());
    }

    @Test
    void testResetPassword_success() throws Exception {

        doNothing().when(passwordAuthService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eMail": "test@gmail.com",
                                  "otp": "123456",
                                  "newPassword": "newPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true))
                .andExpect(jsonPath("$.responseMessage").value("PASSWORD_RESET_SUCCESSFULLY"));

        verify(passwordAuthService, times(1))
                .resetPassword(any());
    }

    @Test
    void testResetPassword_invalidRequest() throws Exception {

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eMail": "",
                                  "otp": "",
                                  "newPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(passwordAuthService, never())
                .resetPassword(any());
    }
}