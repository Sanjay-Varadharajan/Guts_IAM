package com.guts.Guts_IAM.unit_testing.service;

import com.guts.Guts_IAM.auth.dto.ForgotPasswordRequest;
import com.guts.Guts_IAM.auth.dto.ResetPasswordRequest;
import com.guts.Guts_IAM.auth.model.PasswordResetOtp;
import com.guts.Guts_IAM.auth.repository.PasswordResetOtpRepository;
import com.guts.Guts_IAM.auth.service.PasswordAuthService;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PasswordAuthServiceTest {


        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordResetOtpRepository otpRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private EmailService emailService;

        @InjectMocks
        private PasswordAuthService service;

        private User user;

        @BeforeEach
        void setup() {
            MockitoAnnotations.openMocks(this);

            user = new User();
            user.setUserMail("test@mail.com");
            user.setUserPassword("oldPassword");
            user.setTokenVersion(1);
        }

    @Test
    void testForgotPassword_Success() {

        ForgotPasswordRequest req = new ForgotPasswordRequest("test@mail.com");

        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashedOtp");

        service.forgotPassword(req);

        verify(otpRepository).save(any(PasswordResetOtp.class));
        verify(emailService).sendOtp(eq("test@mail.com"), anyString());
    }

    @Test
    void testForgotPassword_UserNotFound() {

        ForgotPasswordRequest req = new ForgotPasswordRequest("wrong@mail.com");

        when(userRepository.findByUserMailAndActiveTrue("wrong@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.forgotPassword(req);
        });
    }
    @Test
    void testForgotPassword_RateLimit() {

        ForgotPasswordRequest req = new ForgotPasswordRequest("test@mail.com");

        PasswordResetOtp existing = new PasswordResetOtp();
        existing.setLastRequestedAt(LocalDateTime.now()); // within 30 sec

        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> {
            service.forgotPassword(req);
        });
    }


    @Test
    void testResetPassword_Success() {

        ResetPasswordRequest req =
                new ResetPasswordRequest("test@mail.com", "123456", "newPass");

        PasswordResetOtp data = new PasswordResetOtp();
        data.setEmail("test@mail.com");
        data.setOtpHash("hashedOtp");
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        data.setAttempts(0);

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.of(data));

        when(passwordEncoder.matches("123456", "hashedOtp"))
                .thenReturn(true);

        when(userRepository.findByUserMailAndActiveTrue("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPass"))
                .thenReturn("encodedPass");

        service.resetPassword(req);

        assertEquals("encodedPass", user.getUserPassword());
        assertEquals(2, user.getTokenVersion());

        verify(userRepository).save(user);
        verify(otpRepository).deleteById("test@mail.com");
    }

    @Test
    void testResetPassword_OtpNotFound() {

        ResetPasswordRequest req =
                new ResetPasswordRequest("test@mail.com", "123456", "newPass");

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.resetPassword(req);
        });
    }

    @Test
    void testResetPassword_Expired() {

        ResetPasswordRequest req =
                new ResetPasswordRequest("test@mail.com", "123456", "newPass");

        PasswordResetOtp data = new PasswordResetOtp();
        data.setExpiryTime(LocalDateTime.now().minusMinutes(1));

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.of(data));

        assertThrows(RuntimeException.class, () -> {
            service.resetPassword(req);
        });

        verify(otpRepository).deleteById("test@mail.com");
    }

    @Test
    void testResetPassword_MaxAttempts() {

        ResetPasswordRequest req =
                new ResetPasswordRequest("test@mail.com", "123456", "newPass");

        PasswordResetOtp data = new PasswordResetOtp();
        data.setAttempts(3);
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.of(data));

        assertThrows(RuntimeException.class, () -> {
            service.resetPassword(req);
        });

        verify(otpRepository).deleteById("test@mail.com");
    }


    @Test
    void testResetPassword_InvalidOtp() {

        ResetPasswordRequest req =
                new ResetPasswordRequest("test@mail.com", "wrongOtp", "newPass");

        PasswordResetOtp data = new PasswordResetOtp();
        data.setOtpHash("hashedOtp");
        data.setAttempts(0);
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findById("test@mail.com"))
                .thenReturn(Optional.of(data));

        when(passwordEncoder.matches("wrongOtp", "hashedOtp"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            service.resetPassword(req);
        });

        assertEquals(1, data.getAttempts());
        verify(otpRepository).save(data);
    }


    }


