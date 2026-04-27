package com.guts.Guts_IAM.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.user.dto.admin.AdminRequestDto;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;

import com.guts.Guts_IAM.user.service.admin.AdminService;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private AuditRepository auditRepository;

        @Mock
        private Principal principal;

        @Mock
        private HttpServletRequest request;

        @InjectMocks
        private AdminService adminService;

        private User adminUser;
        private User normalUser;

        @BeforeEach
        void setup() {
            MockitoAnnotations.openMocks(this);

            adminUser = new User();
            adminUser.setUserId(1);
            adminUser.setUserMail("admin@mail.com");
            Role role=new Role();
            role.setName(Roles.ROLE_USER);
            adminUser.setRoles(Set.of(role));

            normalUser = new User();
            normalUser.setUserId(2);
            normalUser.setUserMail("user@mail.com");
            normalUser.setActive(true);
            Role role1=new Role();
            role1.setName(Roles.ROLE_USER);
            normalUser.setRoles(Set.of(role1));
        }

    @Test
    void testGetAllActiveUsers_Success() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("userMail"));
        Page<User> page = new PageImpl<>(List.of(normalUser));

        when(userRepository.findByActiveTrue(pageable)).thenReturn(page);

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Page<UserResponseDto> result =
                adminService.getAllActiveUsers(principal, pageable, request);

        assertEquals(1, result.getContent().size());
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testGetAllActiveUsers_InvalidSort() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("invalid"));

        assertThrows(Exception.class, () -> {
            adminService.getAllActiveUsers(principal, pageable, request);
        });
    }



    @Test
    void testUpdateUserStatus_Success() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        when(userRepository.findById(2)).thenReturn(Optional.of(normalUser));

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserResponseDto response =
                adminService.updateUserStatus(2, principal, request);

        assertFalse(normalUser.isActive()); // toggled
        verify(userRepository).save(normalUser);
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testUpdateUserStatus_SelfUpdate() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUserStatus(1, principal, request);
        });
    }

    @Test
    void testGetAllAuditLog_Success() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("auditedOn"));
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));

        when(auditRepository.findAll(pageable)).thenReturn(page);

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Page<AuditLogDto> result =
                adminService.getAllAuditLog(principal, pageable, request);

        assertNotNull(result);
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testViewProfile_Success() {

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserResponseDto response =
                adminService.viewProfile(principal, request);

        assertNotNull(response);
        verify(auditRepository).save(any(AuditLog.class));
    }


    @Test
    void testUpdateProfile_Success() {

        AdminRequestDto dto = new AdminRequestDto();
        dto.setAdminName("NewAdmin");

        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("admin@mail.com"))
                .thenReturn(Optional.of(adminUser));

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserResponseDto response =
                adminService.updateProfile(dto, principal, request);

        verify(userRepository).save(adminUser);
        verify(auditRepository).save(any(AuditLog.class));
    }
    }