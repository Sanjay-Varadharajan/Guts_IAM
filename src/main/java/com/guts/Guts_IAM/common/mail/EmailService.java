package com.guts.Guts_IAM.common.mail;


import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender javaMailSender;

    @Value("${iam.support.phone}")
    private String supportPhone;

    @Value("${iam.support.email}")
    private String supportEmail;

    @Async
    public void sendOtp(String toEmail,String otp){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setSubject("Guts_IAM ,Password Reset OTP");
        simpleMailMessage.setText("your otp is: "+otp+" (Valid for 5 minutes)");
        javaMailSender.send(simpleMailMessage);
    }

    @Async
    public void sendUnlockOtp(String email, String otp) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Guts IAM - Unlock Account");
        simpleMailMessage.setText("Your OTP to unlock your account is: " + otp + " (valid for 5 minutes)");
        javaMailSender.send(simpleMailMessage);
    }

    @Async
    public void sendVerificationEmail(
            String toEmail,
            String token
    ) {

        String verificationLink =
                "http://localhost:8080/api/v1/auth/verify-email?token="
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

    @Async
    public void LoginMail(String toEmail, String userName, String ipAddress, GeoLocation location, String device, String browser, String operatingSystem) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(toEmail);
        mail.setSubject("Guts_IAM - Login Successful");
        String message = """
Hello, %s,

We noticed a successful sign-in to your Guts IAM account.

========================================
       LOGIN SECURITY NOTIFICATION
========================================

Login Details
----------------------------------------
Time             : %s
IP Address       : %s
Location         : %s, %s
Device           : %s
Browser          : %s
Operating System : %s

----------------------------------------

If you recognize this activity, no further action is required.

If you did NOT sign in, your account may be at risk.
Please change your password immediately and review your recent account activity.

Need help?
Contact your administrator or the Guts IAM support team immediately.
IAM Support: %s
Email       : %s

Thank you for choosing Guts IAM.

Best regards,

Guts IAM Security Team
""".formatted(
                userName,
                LocalDateTime.now(),
                ipAddress,
                location.getCity(),
                location.getCountry(),
                device,
                browser,
                operatingSystem,
                supportPhone,
                supportEmail
        );

        mail.setText(message);

        javaMailSender.send(mail);
    }

    }



