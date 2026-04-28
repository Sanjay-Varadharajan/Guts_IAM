package com.guts.Guts_IAM.security;

import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetailService;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailService customUserDetailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================
    // VALID TOKEN FLOW
    // =========================
    @Test
    void shouldAuthenticateWhenValidTokenPresent() throws Exception {

        String token = "valid.jwt.token";
        String username = "broUser";

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(request.getServletPath()).thenReturn("/api/user/profile");

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtils.getUsernameFromToken(token))
                .thenReturn(username);

        when(customUserDetailService.loadUserByUsername(username))
                .thenReturn(userDetails);

        when(userDetails.getAuthorities())
                .thenReturn(Collections.emptyList());

        // 🔥 THIS WAS MISSING
        when(userDetails.getUsername()).thenReturn(username);

        filter.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertEquals(username, auth.getName());
        assertTrue(auth.isAuthenticated());

        verify(filterChain).doFilter(request, response);
    }

    // =========================
    // NO AUTH HEADER
    // =========================
    @Test
    void shouldSkipWhenNoAuthorizationHeader() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        when(request.getServletPath())
                .thenReturn("/api/user/profile");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(customUserDetailService);
    }

    // =========================
    // INVALID TOKEN
    // =========================
    @Test
    void shouldNotAuthenticateWhenTokenInvalid() throws Exception {

        String token = "bad.token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtils.getUsernameFromToken(token))
                .thenThrow(new RuntimeException("Invalid token"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    // =========================
    // AUTH ENDPOINT SKIP FLOW
    // =========================
    @Test
    void shouldSkipJwtProcessingForAuthEndpoints() throws Exception {

        when(request.getServletPath())
                .thenReturn("/api/auth/login");

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(customUserDetailService);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // =========================
    // NON-AUTH ENDPOINT PROCESSED
    // =========================
    @Test
    void shouldProcessNonAuthEndpointsNormally() throws Exception {

        when(request.getServletPath())
                .thenReturn("/api/user/dashboard");

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

}