package com.guts.Guts_IAM.unit_testing.security;

import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetailService;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService userDetailService;

    @Test
    void shouldLoadUserByUsernameSuccessfully() {

        // Arrange
        Role role = mock(Role.class);
        when(role.getName()).thenReturn("ROLE_USER");

        User user = mock(User.class);
        when(user.getUserMail()).thenReturn("test@mail.com");
        when(user.getUserPassword()).thenReturn("password123");
        when(user.isActive()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(role));

        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails result = userDetailService.loadUserByUsername("test@mail.com");

        // Assert
        assertEquals("test@mail.com", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertTrue(result.isEnabled());

        assertEquals(1, result.getAuthorities().size());
        assertEquals("ROLE_USER",
                result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByUserMailAndActiveTrue("missing@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("missing@mail.com")
        );
    }
}
