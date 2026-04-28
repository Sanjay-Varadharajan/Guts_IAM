package com.guts.Guts_IAM.common;


import com.guts.Guts_IAM.common.mail.EmailService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {


    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendOtp() {

        String email = "test@guts.com";
        String otp = "123456";

        emailService.sendOtp(email, otp);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(javaMailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMail = captor.getValue();

        assertEquals(email, sentMail.getTo()[0]);
        assertEquals("Guts_IAM ,Password Reset OTP", sentMail.getSubject());
        assertTrue(sentMail.getText().contains(otp));
        assertTrue(sentMail.getText().contains("Valid for 5 minutes"));
    }

    @Test
    void testSendUnlockOtp() {

        String email = "unlock@guts.com";
        String otp = "999999";

        emailService.sendUnlockOtp(email, otp);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(javaMailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMail = captor.getValue();

        assertEquals(email, sentMail.getTo()[0]);
        assertEquals("Guts IAM - Unlock Account", sentMail.getSubject());
        assertTrue(sentMail.getText().contains(otp));
        assertTrue(sentMail.getText().contains("unlock your account"));
    }

    @Test
    void testNoEmailSentWithoutCall() {

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}


