package com.guts.Guts_IAM.unit_testing.controller;

import com.guts.Guts_IAM.auth.controller.AccountUnlockController;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.auth.service.UnlockAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AccountUnlockController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountUnlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnlockAccountService unlockAccountService;

    @MockBean
    private com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter jwtAuthenticationFilter;


    @Test
    void testRequestOtp() throws Exception {

        doNothing().when(unlockAccountService)
                .sendUnlockOtp(anyString());

        mockMvc.perform(post("/api/unlock/request")
                        .param("email", "test@gmail.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent"));

        verify(unlockAccountService, times(1))
                .sendUnlockOtp("test@gmail.com");
    }

    @Test
    void testVerifyOtp() throws Exception {
        JwtResponse mockResponse =
                new JwtResponse("token123", "refresh123", "USER");

        when(unlockAccountService.verifyOtpAndUnlock(
                anyString(),
                anyString(),
                any()
        )).thenReturn(mockResponse);

        mockMvc.perform(post("/api/unlock/verify")
                        .param("email", "test@gmail.com")
                        .param("otp", "123456")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"))
                .andExpect(jsonPath("$.tokenType").value("USER"));

        verify(unlockAccountService, times(1))
                .verifyOtpAndUnlock(eq("test@gmail.com"),
                        eq("123456"),
                        any());
    }
}