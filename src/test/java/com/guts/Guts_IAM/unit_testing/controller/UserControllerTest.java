package com.guts.Guts_IAM.unit_testing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.user.controller.user.UserController;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    // -----------------------------
    // 1. VIEW PROFILE
    // -----------------------------
    @Test
    void testViewProfile() throws Exception {

        Authentication auth =
                new UsernamePasswordAuthenticationToken("user123", null);

        UserResponseDto response = new UserResponseDto();

        when(userService.viewProfile(any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/user/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true));
    }

    // -----------------------------
    // 2. UPDATE PROFILE
    // -----------------------------
    @Test
    void testUpdateProfile() throws Exception {

        Authentication auth =
                new UsernamePasswordAuthenticationToken("user123", null);

        UserRequestDto requestDto = new UserRequestDto();
        UserResponseDto responseDto = new UserResponseDto();

        when(userService.updateProfile(any(), any(), any()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/user/me/update")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true))
                .andExpect(jsonPath("$.responseMessage").value("PROFILE_UPDATED"));
    }

    // -----------------------------
    // 3. VIEW LOGS
    // -----------------------------
    @Test
    void testViewLogs() throws Exception {

        Authentication auth =
                new UsernamePasswordAuthenticationToken("user123", null);

        Page<AuditLogDtoForUser> page =
                new PageImpl<>(List.of(new AuditLogDtoForUser()));

        when(userService.viewLogs(any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/user/me/logs")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true))
                .andExpect(jsonPath("$.responseMessage").value("USER_AUDIT_LOG"));
    }
}