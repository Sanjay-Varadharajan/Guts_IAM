package com.guts.Guts_IAM.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.user.controller.admin.AdminController;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.service.admin.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.security.Principal;
import static org.mockito.Mockito.mock;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HttpServletRequest httpServletRequest;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllActiveUsers() throws Exception {

        Page<UserResponseDto> page = new PageImpl<>(List.of(new UserResponseDto()));

        when(adminService.getAllActiveUsers(any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseSuccess").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserStatus() throws Exception {

        when(adminService.updateUserStatus(any(), any(), any()))
                .thenReturn(new UserResponseDto());

        mockMvc.perform(patch("/api/admin/user/1/status"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogs() throws Exception {

        Page<AuditLogDto> page = new PageImpl<>(List.of(new AuditLogDto()));

        when(adminService.getAllAuditLog(any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/logs/viewall"))
                .andExpect(status().isOk());
    }

    @Test
    void testViewProfile() throws Exception {

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        when(adminService.viewProfile(auth,httpServletRequest))
                .thenReturn(new UserResponseDto());

        mockMvc.perform(get("/api/admin/me")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProfile() throws Exception {

        when(adminService.updateProfile(any(), any(), any()))
                .thenReturn(new UserResponseDto());

        mockMvc.perform(patch("/api/admin/me/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}