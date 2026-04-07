package com.guts.Guts_IAM.mail.service;


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


    }



