package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.auth.dto.ForgotPasswordRequest;
import com.guts.Guts_IAM.auth.model.PasswordResetOtp;
import com.guts.Guts_IAM.auth.repository.PasswordResetOtpRepository;
import com.guts.Guts_IAM.auth.dto.ResetPasswordRequest;
import com.guts.Guts_IAM.common.exception.types.TokenNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.passwordtracking.service.PasswordTrackerService;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.common.util.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordAuthService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final PasswordTrackerService passwordTrackerService;
    private static final int OTP_LENGTH = 6;


    public void forgotPassword(ForgotPasswordRequest req, HttpServletRequest httpServletRequest) {

        Optional<User> optionalUser =
                userRepository.findByUserMailAndActiveTrue(req.eMail());

        if (optionalUser.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.FORGOT_PASSWORD,
                    "AUTH",
                    req.eMail(),
                    AuditStatus.FAILED,
                    "Password reset requested for unknown email",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    "UserName Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        User user = optionalUser.get();

        PasswordResetOtp existing = otpRepository.findById(req.eMail()).orElse(null);

        if (existing != null && existing.getLastRequestedAt() != null &&
                existing.getLastRequestedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            auditLogService.log(
                    user,
                    Action.SEND_OTP,
                    "AUTH",
                    user.getUserId().toString(),
                    AuditStatus.FAILED,
                    "Password reset OTP requested too frequently",
                    httpServletRequest
            );
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
        auditLogService.log(
                user,
                Action.SEND_OTP,
                "AUTH",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Password reset OTP sent successfully",
                httpServletRequest
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req, HttpServletRequest httpServletRequest) {

        Optional<PasswordResetOtp> optionalOtp =
                otpRepository.findById(req.email());

        Optional<User> optionalUser =
                userRepository.findByUserMailAndActiveTrue(req.email());

        if (optionalUser.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.FORGOT_PASSWORD,
                    "AUTH",
                    req.email(),
                    AuditStatus.FAILED,
                    "Password reset attempted for unknown email",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    "UserName Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        User user = optionalUser.get();

        if (optionalOtp.isEmpty()) {

            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    req.email(),
                    AuditStatus.FAILED,
                    "Password reset OTP not found",
                    httpServletRequest
            );

            throw new TokenNotFoundException(
                    "Otp not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        PasswordResetOtp data = optionalOtp.get();

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {

            otpRepository.deleteById(req.email());

            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    req.email(),
                    AuditStatus.EXPIRED,
                    "Expired password reset OTP used",
                    httpServletRequest
            );

            throw new RuntimeException("OTP expired");
        }

        if (data.getAttempts() >= 3) {

            otpRepository.deleteById(req.email());

            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    req.email(),
                    AuditStatus.LOCKED,
                    "Too many invalid password reset OTP attempts",
                    httpServletRequest
            );

            throw new RuntimeException("Too many attempts");
        }

        if (!passwordEncoder.matches(req.otp(), data.getOtpHash())) {

            data.setAttempts(data.getAttempts() + 1);
            otpRepository.save(data);

            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    req.email(),
                    AuditStatus.FAILED,
                    "Invalid password reset OTP entered",
                    httpServletRequest
            );

            throw new RuntimeException("Invalid OTP");
        }

        String encodedPassword = passwordEncoder.encode(req.newPassword());

        user.setUserPassword(encodedPassword);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        auditLogService.log(
                user,
                Action.RESET_PASSWORD,
                "AUTH",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Password reset successfully",
                httpServletRequest
        );

        passwordTrackerService.trackChange(encodedPassword,user);
        otpRepository.deleteById(req.email());
    }
}