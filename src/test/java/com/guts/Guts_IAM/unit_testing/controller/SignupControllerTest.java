package com.guts.Guts_IAM.unit_testing.controller;

import com.guts.Guts_IAM.auth.controller.SignupController;
import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.auth.service.SignupService;
import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignupController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupService signupService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testSignup_success() throws Exception {

        ApiResponse mockResponse =
                new ApiResponse(
                        true,
                        "Verification email sent successfully",
                        null,
                        LocalDateTime.now()
                );

        when(
                signupService.signup(
                        any(SignupRequest.class),
                        any(HttpServletRequest.class)
                )
        ).thenReturn(mockResponse);

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "userMail": "test@gmail.com",
                                  "userName": "sanjay",
                                  "userPassword": "pass123"
                                }
                                """)
                )

                .andExpect(status().isCreated())

                .andExpect(
                        jsonPath("$.responseSuccess")
                                .value(true)
                )

                .andExpect(
                        jsonPath("$.responseMessage")
                                .value(
                                        "Verification email sent successfully"
                                )
                );

        verify(signupService, times(1))
                .signup(
                        any(SignupRequest.class),
                        any(HttpServletRequest.class)
                );
    }

    @Test
    void testSignup_invalidRequest() throws Exception {

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "userMail": "",
                                  "userName": "",
                                  "userPassword": ""
                                }
                                """)
                )

                .andExpect(status().isBadRequest());

        verify(signupService, never())
                .signup(any(), any());
    }
}