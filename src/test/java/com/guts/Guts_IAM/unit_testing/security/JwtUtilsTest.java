package com.guts.Guts_IAM.unit_testing.security;

import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder()
                .encodeToString("this-is-a-super-secret-key-123456".getBytes());

        jwtUtils = new JwtUtils(secret);
    }

    // =========================
    // ACCESS TOKEN TEST
    // =========================
    @Test
    void shouldGenerateAccessToken() {

        Role role = mock(Role.class);
        when(role.getName()).thenReturn(Roles.ROLE_USER);

        when(user.getUserMail()).thenReturn("test@mail.com");
        when(user.getRoles()).thenReturn(Set.of(role));

        String token = jwtUtils.generateAccessToken(user);

        assertNotNull(token);

        // username check
        assertEquals("test@mail.com", jwtUtils.getUsernameFromToken(token));

        // roles check (ENUM stored as string in JWT)
        List<String> roles = jwtUtils.getRolesFromToken(token);

        assertTrue(roles.contains(Roles.ROLE_USER.name()));
    }

    // =========================
    // REFRESH TOKEN TEST
    // =========================
    @Test
    void shouldGenerateRefreshToken() {

        when(user.getUserMail()).thenReturn("test@mail.com");

        String token = jwtUtils.generateRefreshToken(user);

        assertNotNull(token);

        assertEquals("test@mail.com", jwtUtils.getUsernameFromToken(token));
    }

    // =========================
    // VALID TOKEN TEST
    // =========================
    @Test
    void shouldValidateValidToken() {

        Role role = mock(Role.class);
        when(role.getName()).thenReturn(Roles.ROLE_USER);

        when(user.getUserMail()).thenReturn("test@mail.com");
        when(user.getRoles()).thenReturn(Set.of(role));

        String token = jwtUtils.generateAccessToken(user);

        assertTrue(jwtUtils.validateToken(token));
    }

    // =========================
    // INVALID TOKEN TEST
    // =========================
    @Test
    void shouldInvalidateFakeToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.value"));
    }

    // =========================
    // EXPIRY TEST
    // =========================
    @Test
    void shouldReturnRefreshTokenExpiry() {

        assertEquals(
                7 * 24 * 60 * 60 * 1000L,
                jwtUtils.getRefreshTokenExpiry()
        );
    }
}