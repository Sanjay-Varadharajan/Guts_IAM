package com.guts.Guts_IAM.service;

import com.guts.Guts_IAM.auth.dto.ForgotPasswordRequest;
import com.guts.Guts_IAM.auth.model.PasswordResetOtp;
import com.guts.Guts_IAM.auth.repository.PasswordResetOtpRepository;
import com.guts.Guts_IAM.auth.dto.ResetPasswordRequest;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.common.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordAuthService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;



    public void forgotPassword(ForgotPasswordRequest req) {

        User user = userRepository.findByUserMailAndActiveTrue(req.eMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetOtp existing = otpRepository.findById(req.eMail()).orElse(null);

        if (existing != null && existing.getLastRequestedAt() != null &&
                existing.getLastRequestedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Wait before requesting again");
        }

        String otp = OtpUtil.generateOtp(OTP_LENGTH);

        PasswordResetOtp data = new PasswordResetOtp();
        data.setEmail(req.eMail());
        data.setOtpHash(passwordEncoder.encode(otp));
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        data.setAttempts(0);
        data.setLastRequestedAt(LocalDateTime.now());

        otpRepository.save(data);

        emailService.sendOtp(req.eMail(), otp);
    }

    public void resetPassword(ResetPasswordRequest req) {

        PasswordResetOtp data = otpRepository.findById(req.email())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.deleteById(req.email());
            throw new RuntimeException("OTP expired");
        }

        if (data.getAttempts() >= 3) {
            otpRepository.deleteById(req.email());
            throw new RuntimeException("Too many attempts");
        }

        if (!passwordEncoder.matches(req.otp(), data.getOtpHash())) {
            data.setAttempts(data.getAttempts() + 1);
            otpRepository.save(data);
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByUserMailAndActiveTrue(req.email())
                .orElseThrow();

        user.setUserPassword(passwordEncoder.encode(req.newPassword()));

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        otpRepository.deleteById(req.email());
    }


}