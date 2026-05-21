package com.guts.Guts_IAM.unit_testing.security;

import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void shouldReturnCorrectUserDetails() {

        CustomUserDetails userDetails =
                new CustomUserDetails(
                        "broUser",
                        "secret123",
                        true,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        assertEquals("broUser", userDetails.getUsername());
        assertEquals("secret123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER",
                userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldReturnAccountFlagsTrue() {

        CustomUserDetails userDetails =
                new CustomUserDetails(
                        "test",
                        "pass",
                        true,
                        List.of()
                );

        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}