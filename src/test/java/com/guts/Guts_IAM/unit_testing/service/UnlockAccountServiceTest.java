package com.guts.Guts_IAM.unit_testing.service;

import com.guts.Guts_IAM.auth.model.AccountUnlockOtp;
import com.guts.Guts_IAM.auth.repository.AccountUnlockRepository;
import com.guts.Guts_IAM.auth.service.AuthService;
import com.guts.Guts_IAM.auth.service.UnlockAccountService;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class UnlockAccountServiceTest {
    @Mock
    private AccountUnlockRepository accountUnlockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UnlockAccountService unlockAccountService;

    @Test
    void sendUnlockOtp_success() {
        String email = "test@gmail.com";

        User user = new User();
        user.setAccountNonLocked(false);

        when(userRepository.findByUserMailAndActiveTrue(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

        unlockAccountService.sendUnlockOtp(email,httpServletRequest);

        verify(accountUnlockRepository).save(any(AccountUnlockOtp.class));
        verify(emailService).sendUnlockOtp(eq(email), anyString());
    }

    @Test
    void sendUnlockOtp_userNotFound() {

        String email = "test@gmail.com";

        when(userRepository.findByUserMailAndActiveTrue(email))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            unlockAccountService.sendUnlockOtp(email,httpServletRequest);
        });
    }

    @Test
    void sendUnlockOtp_accountNotLocked() {

        String email = "test@gmail.com";

        User user = new User();
        user.setAccountNonLocked(true);

        when(userRepository.findByUserMailAndActiveTrue(email))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> {
            unlockAccountService.sendUnlockOtp(email,httpServletRequest);
        });
    }

    @Test
    void verifyOtpAndUnlock_success() {

        String email = "test@gmail.com";
        String otp = "123456";

        AccountUnlockOtp data = new AccountUnlockOtp();
        data.setOtpHash("hashedOtp");
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        data.setUnlockAttempts(0);

        User user = new User();
        user.setTokenVersion(1);

        when(accountUnlockRepository.findById(email))
                .thenReturn(Optional.of(data));

        when(passwordEncoder.matches(otp, "hashedOtp"))
                .thenReturn(true);

        when(userRepository.findByUserMailAndActiveTrue(email))
                .thenReturn(Optional.of(user));

        when(jwtUtils.generateAccessToken(user))
                .thenReturn("accessToken");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refreshToken");

        when(authService.createRefreshToken(user,))
                .thenReturn(refreshToken);

        JwtResponse response = unlockAccountService
                .verifyOtpAndUnlock(email, otp, null);

        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userRepository).save(user);
        verify(accountUnlockRepository).deleteById(email);
    }

    @Test
    void verifyOtp_invalidOtp() {

        String email = "test@gmail.com";
        String otp = "123456";

        AccountUnlockOtp data = new AccountUnlockOtp();
        data.setOtpHash("hashedOtp");
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        data.setUnlockAttempts(0);

        when(accountUnlockRepository.findById(email))
                .thenReturn(Optional.of(data));

        when(passwordEncoder.matches(otp, "hashedOtp"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            unlockAccountService.verifyOtpAndUnlock(email, otp, null);
        });

        verify(accountUnlockRepository).save(data);
    }

    @Test
    void verifyOtp_expired() {

        String email = "test@gmail.com";

        AccountUnlockOtp data = new AccountUnlockOtp();
        data.setExpiryTime(LocalDateTime.now().minusMinutes(1));

        when(accountUnlockRepository.findById(email))
                .thenReturn(Optional.of(data));

        assertThrows(RuntimeException.class, () -> {
            unlockAccountService.verifyOtpAndUnlock(email, "123456", null);
        });

        verify(accountUnlockRepository).deleteById(email);
    }
}
