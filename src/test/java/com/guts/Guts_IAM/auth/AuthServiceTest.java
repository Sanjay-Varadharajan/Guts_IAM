package com.guts.Guts_IAM.auth;

import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.auditlog.service.AuditService;
import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.auth.service.AuthService;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.token.audit.TokenAuditRepository;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.token.refreshtoken.repository.RefreshTokenRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {


    @Mock
    AuthenticationManager authManager;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    TokenAuditRepository tokenAuditRepository;
    @Mock
    AuditRepository auditRepo;
    @Mock
    AuditService auditService;
    @InjectMocks
    AuthService authService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void login_success() {

        LoginRequest request = new LoginRequest();
        request.setUserMail("sanjay@gmail.com");
        request.setUserPassword("123");

        User user = new User();
        user.setUserId(1);
        user.setUserMail("sanjay@gmail.com");
        user.setAccountNonLocked(true);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        roles.add(role);

        user.setRoles(roles);

        when(userRepository.findByUserMailAndActiveTrue("sanjay@gmail.com"))
                .thenReturn(Optional.of(user));

        when(authManager.authenticate(any()))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));

        when(jwtUtils.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtils.getRefreshTokenExpiry()).thenReturn(100000L);

        when(refreshTokenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");

        JwtResponse response = authService.login(request, httpRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());

        verify(userRepository).save(user); // failedAttempts reset
        verify(tokenAuditRepository).save(any());
        verify(auditRepo).save(any());
    }

    @Test
    void login_invalidPassword_attempt1() {

        LoginRequest request = new LoginRequest();
        request.setUserMail("sanjay@gmail.com");
        request.setUserPassword("wrong");

        User user = new User();
        user.setUserId(1);
        user.setUserMail("sanjay@gmail.com");
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        roles.add(role);

        user.setRoles(roles);

        when(userRepository.findByUserMailAndActiveTrue(any()))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException()).when(authManager).authenticate(any());

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.login(request, httpRequest)
        );

        assertEquals("Invalid credentials. Attempt 1/3", ex.getMessage());

        verify(userRepository).save(user);
    }

    @Test
    void login_accountLocked_after3Attempts() {

        LoginRequest request = new LoginRequest();
        request.setUserMail("sanjay@gmail.com");
        request.setUserPassword("wrong");

        User user = new User();
        user.setUserId(1);
        user.setUserMail("sanjay@gmail.com");
        user.setAccountNonLocked(true);
        user.setFailedAttempts(2);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        roles.add(role);

        user.setRoles(roles);

        when(userRepository.findByUserMailAndActiveTrue(any()))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException()).when(authManager).authenticate(any());

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.login(request, httpRequest)
        );

        assertTrue(ex.getMessage().contains("Account locked"));

        verify(auditService).saveAudit(any());
    }

    @Test
    void logout_success() {

        String token = "refresh123";

        User user = new User();
        user.setUserId(1);
        user.setUserMail("sanjay@gmail.com");
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        roles.add(role);

        user.setRoles(roles);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        authService.logout(token, request);

        verify(refreshTokenRepository).deleteByToken(token);
        verify(auditRepo).save(any());
    }

    @Test
    void logout_tokenNotFound() {

        when(refreshTokenRepository.findByToken("invalid"))
                .thenReturn(Optional.empty());

        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(ResourceNotFoundException.class, () ->
                authService.logout("invalid", request)
        );
    }


    @Test
    void updateFailedAttempts_shouldLockAccount() {

        User user = new User();
        user.setFailedAttempts(2);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Roles.ROLE_USER);
        roles.add(role);

        user.setRoles(roles);
        int attempts = authService.updateFailedAttempts(user);

        assertEquals(3, attempts);
        assertFalse(user.isAccountNonLocked());

        verify(userRepository).save(user);
    }


}
