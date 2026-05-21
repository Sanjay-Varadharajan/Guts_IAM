package com.guts.Guts_IAM.unit_testing.controller;

import com.guts.Guts_IAM.auth.controller.SignupController;
import com.guts.Guts_IAM.auth.service.SignupService;
import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        SignupRequest mockResponse = new SignupRequest(
                "test@gmail.com",
                "sanjay",
                "pass123"
        );

        when(signupService.signup(any(SignupRequest.class), any(HttpServletRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userMail": "test@gmail.com",
                              "userName": "sanjay",
                              "userPassword": "pass123"
                            }
                            """))
            .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseBody.userName").value("test@gmail.com"))
            .andExpect(jsonPath("$.responseSuccess").value(true))
                .andExpect(jsonPath("$.responseMessage").value("Signed Up Successfully"));


        verify(signupService, times(1))
                .signup(any(SignupRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void testSignup_invalidRequest() throws Exception {

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userMail": "",
                                  "userName": "",
                                  "userPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(signupService, never())
                .signup(any(), any());
    }
}