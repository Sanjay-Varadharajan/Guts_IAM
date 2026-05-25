package com.guts.Guts_IAM.unit_testing.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.auth.service.SignupService;
import com.guts.Guts_IAM.common.exception.types.ConflictException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SignUpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SignupService signupService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    public void signupTest() {

        SignupRequest signupRequest =
                new SignupRequest();

        signupRequest.setUserName("sanjay");

        signupRequest.setUserMail(
                "sanjay@gmail.com"
        );

        signupRequest.setUserPassword(
                "123456"
        );

        when(
                userRepository.findByUserMailAndActiveTrue(
                        "sanjay@gmail.com"
                )
        ).thenReturn(Optional.empty());

        when(
                bCryptPasswordEncoder.encode(
                        "123456"
                )
        ).thenReturn("hashedPassword");

        HttpServletRequest httpServletRequest =
                mock(HttpServletRequest.class);

        when(
                httpServletRequest.getRemoteAddr()
        ).thenReturn("127.0.0.1");

        when(
                httpServletRequest.getHeader(
                        "User-Agent"
                )
        ).thenReturn("JUnit");

        ApiResponse result =
                signupService.signup(
                        signupRequest,
                        httpServletRequest
                );

        assertNotNull(result);

        assertEquals(
                true,
                result.isResponseSuccess()
        );

        assertEquals(
                "Verification email sent successfully",
                result.getResponseMessage()
        );

        verify(bCryptPasswordEncoder)
                .encode("123456");

        verify(valueOperations, times(1))
                .set(
                        startsWith("signup:"),
                        any(),
                        any()
                );

        verify(emailService, times(1))
                .sendVerificationEmail(
                        eq("sanjay@gmail.com"),
                        anyString()
                );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(
                        AuditLog.class
                );

        verify(auditRepository)
                .save(captor.capture());

        AuditLog log =
                captor.getValue();

        assertEquals(
                "SIGN_UP_PENDING",
                log.getLogAction()
        );

        assertEquals(
                "sanjay@gmail.com",
                log.getUserMail()
        );

        assertEquals(
                "AUTH",
                log.getResource()
        );

        assertEquals(
                "127.0.0.1",
                log.getIpAddress()
        );

        assertEquals(
                "JUnit",
                log.getUserAgent()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    public void signupTest_userAlreadyExists() {

        SignupRequest request =
                new SignupRequest();

        request.setUserMail(
                "sanjay@gmail.com"
        );

        when(
                userRepository.findByUserMailAndActiveTrue(
                        "sanjay@gmail.com"
                )
        ).thenReturn(Optional.of(new User()));

        HttpServletRequest httpRequest =
                mock(HttpServletRequest.class);

        assertThrows(
                ConflictException.class,
                () -> signupService.signup(
                        request,
                        httpRequest
                )
        );

        verify(valueOperations, never())
                .set(any(), any(), any());

        verify(emailService, never())
                .sendVerificationEmail(
                        anyString(),
                        anyString()
                );
    }
}