package com.guts.Guts_IAM.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.common.exception.types.ConflictException;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.role.repository.RoleRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;


public class SignUpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private SignupService signupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void signupTest(){

        User user=new User();
        user.setUserName("sanjay");
        user.setUserMail("sanjay@gmail.com");
        user.setUserPassword("123456");


        SignupRequest signupRequest=new SignupRequest(user);

        when(userRepository.findByUserMailAndActiveTrue("sanjay@gmail.com"))
                .thenReturn(Optional.empty());

        when(bCryptPasswordEncoder.encode("123456")).thenReturn("123456");

        Role role=new Role();
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(role));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setUserId(1);
                    return u;
                });

        HttpServletRequest httpServletRequest=mock(HttpServletRequest.class);

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");

        SignupRequest result = signupService.signup(signupRequest, httpServletRequest);
        assertNotNull(result);
        assertEquals("sanjay@gmail.com", result.getUserMail());

        verify(userRepository, times(1)).save(any(User.class));
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditRepository).save(captor.capture());

        AuditLog log = captor.getValue();

        verify(bCryptPasswordEncoder).encode("123456");
        assertEquals("SIGN_UP", log.getLogAction());
        assertEquals("sanjay@gmail.com", log.getUserMail());
        assertEquals("AUTH", log.getResource());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals("JUnit", log.getUserAgent());}

    @Test
    public void signupTest_userAlreadyExists() {

        SignupRequest request = new SignupRequest();
        request.setUserMail("sanjay@gmail.com");

        when(userRepository.findByUserMailAndActiveTrue("sanjay@gmail.com"))
                .thenReturn(Optional.of(new User()));

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThrows(ConflictException.class, () -> {
            signupService.signup(request, httpRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
