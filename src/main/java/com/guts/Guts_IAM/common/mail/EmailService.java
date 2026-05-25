package com.guts.Guts_IAM.common.mail;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender javaMailSender;
    
    public void sendOtp(String toEmail,String otp){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setSubject("Guts_IAM ,Password Reset OTP");
        simpleMailMessage.setText("your otp is: "+otp+" (Valid for 5 minutes)");
        javaMailSender.send(simpleMailMessage);
    }

    public void sendUnlockOtp(String email, String otp) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Guts IAM - Unlock Account");
        simpleMailMessage.setText("Your OTP to unlock your account is: " + otp + " (valid for 5 minutes)");
        javaMailSender.send(simpleMailMessage);
    }

    public void sendVerificationEmail(
            String toEmail,
            String token
    ) {

        String verificationLink =
                "http://localhost:8080/api/auth/verify-email?token="
                        + token;

        SimpleMailMessage simpleMailMessage =
                new SimpleMailMessage();

        simpleMailMessage.setTo(toEmail);

        simpleMailMessage.setSubject(
                "Guts IAM - Email Verification"
        );

        simpleMailMessage.setText(
                "Click the link below to verify your email:\n\n"
                        + verificationLink
                        + "\n\nValid for 15 minutes."
        );

        javaMailSender.send(simpleMailMessage);
    }


    }



