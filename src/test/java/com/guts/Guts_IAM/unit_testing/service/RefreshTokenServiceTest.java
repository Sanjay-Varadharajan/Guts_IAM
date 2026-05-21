package com.guts.Guts_IAM.unit_testing.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.token.audit.TokenAudit;
import com.guts.Guts_IAM.token.audit.TokenAuditRepository;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.token.refreshtoken.repository.RefreshTokenRepository;
import com.guts.Guts_IAM.token.service.RefreshTokenService;
import com.guts.Guts_IAM.user.model.User;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RefreshTokenServiceTest {

        @Mock
        private RefreshTokenRepository refreshTokenRepository;

        @Mock
        private JwtUtils jwtUtils;

        @Mock
        private AuditRepository auditRepository;

        @Mock
        private TokenAuditRepository tokenAuditRepository;

        @Mock
        private HttpServletRequest request;

        @InjectMocks
        private RefreshTokenService refreshTokenService;

        private RefreshToken refreshToken;
        private User user;

        @BeforeEach
        void setup() {
            MockitoAnnotations.openMocks(this);

            user = new User();
            user.setUserId(1);
            user.setUserMail("test@mail.com");
            Role role=new Role();
            role.setName(Roles.ROLE_USER);
            user.setRoles(Set.of(role));

            refreshToken = new RefreshToken();
            refreshToken.setToken("refresh123");
            refreshToken.setUser(user);
        }

    @Test
    void testRefreshAccessToken_Success() {

        refreshToken.setExpiryDate(Date.from(Instant.now().plusSeconds(3600)));

        when(refreshTokenRepository.findByToken("refresh123"))
                .thenReturn(Optional.of(refreshToken));

        when(jwtUtils.generateAccessToken(user))
                .thenReturn("newAccessToken");

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");

        JwtResponse response =
                refreshTokenService.refreshAccessToken("refresh123", request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());

        verify(tokenAuditRepository).save(any(TokenAudit.class));
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void testRefreshAccessToken_TokenNotFound() {

        when(refreshTokenRepository.findByToken("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            refreshTokenService.refreshAccessToken("invalid", request);
        });
    }

    @Test
    void testRefreshAccessToken_ExpiredToken() {

        refreshToken.setExpiryDate(Date.from(Instant.now().minusSeconds(10)));

        when(refreshTokenRepository.findByToken("refresh123"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(RuntimeException.class, () -> {
            refreshTokenService.refreshAccessToken("refresh123", request);
        });

        verify(refreshTokenRepository).delete(refreshToken);
    }


    }


