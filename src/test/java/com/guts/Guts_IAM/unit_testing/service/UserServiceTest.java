package com.guts.Guts_IAM.unit_testing.service;

import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.user.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1);
        user.setUserMail("test@mail.com");
        user.setUserName("OldName");
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        user.setRoles(Set.of(role));
    }

    @Test
    void testViewProfile_Success() {

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserResponseDto response = userService.viewProfile(authentication, request);

        assertNotNull(response);
        verify(auditRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testViewProfile_UserNotFound() {

        when(authentication.getName()).thenReturn("wrong@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("wrong@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            userService.viewProfile(authentication, request);
        });
    }

    @Test
    void testUpdateProfile_Success() {

        UserRequestDto dto = new UserRequestDto();
        dto.setUserName("NewName");

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserResponseDto response = userService.updateProfile(dto, authentication, request);

        assertEquals("NewName", user.getUserName());
        verify(userRepository).save(user);
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testViewLogs_Success() {

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("auditedOn"));

        List<AuditLog> logs = List.of(new AuditLog());
        Page<AuditLog> page = new PageImpl<>(logs);

        when(auditRepository.findByUserMail(pageable, "test@mail.com"))
                .thenReturn(page);

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Page<AuditLogDtoForUser> result =
                userService.viewLogs(authentication, pageable, request);

        assertNotNull(result);
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testViewLogs_InvalidSort() {

        when(authentication.getName()).thenReturn("test@mail.com");
        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("invalidField"));

        assertThrows(Exception.class, () -> {
            userService.viewLogs(authentication, pageable, request);
        });
    }
}
