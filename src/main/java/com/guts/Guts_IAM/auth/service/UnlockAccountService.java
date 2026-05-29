package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.auth.model.AccountUnlockOtp;
import com.guts.Guts_IAM.auth.repository.AccountUnlockRepository;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.common.util.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnlockAccountService {

    private final AccountUnlockRepository accountUnlockRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    private static final int OTP_LENGTH = 6;

    public void sendUnlockOtp(String email,HttpServletRequest httpServletRequest) {

        Optional<User> optionalUser =
                userRepository.findByUserMailAndActiveTrue(email);

        if(optionalUser.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.ACCOUNT_UNLOCK,
                    "AUTH",
                    "UNKNOWN",
                    AuditStatus.FAILED,
                    "OTP unlock requested for unknown email: " + email,
                    httpServletRequest
            );

            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();
        if (user.isAccountNonLocked()) {
            throw new LockedException("Account is not locked");
        }

        AccountUnlockOtp existing = accountUnlockRepository.findById(email).orElse(null);

        if (existing != null &&
                existing.getLastRequestedAt() != null &&
                existing.getLastRequestedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {

            auditLogService.log(
                    user,
                    Action.ACCOUNT_UNLOCK,
                    "AUTH",
                    user.getUserId().toString(),
                    AuditStatus.FAILED,
                    "Unlock OTP requested too frequently",
                    httpServletRequest
            );

            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting OTP again"
            );

        }

        String otp = OtpUtil.generateOtp(OTP_LENGTH);

        AccountUnlockOtp data = new AccountUnlockOtp();
        data.setEMail(email);
        data.setOtpHash(passwordEncoder.encode(otp));
        data.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        data.setUnlockAttempts(0);
        data.setLastRequestedAt(LocalDateTime.now());

        accountUnlockRepository.save(data);

        emailService.sendUnlockOtp(email, otp);
        auditLogService.log(
                user,
                Action.SEND_OTP,
                "AUTH",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Account unlock OTP sent successfully",
                httpServletRequest
        );

    }

    public JwtResponse verifyOtpAndUnlock(String email,
                                          String otpInput,
                                          HttpServletRequest request) {

        Optional<User> optionalUser =
                userRepository.findByUserMailAndActiveTrue(email);

        if(optionalUser.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.VERIFY_OTP,
                    "AUTH",
                    "UNKNOWN",
                    AuditStatus.FAILED,
                    "OTP verify requested for unknown email: " + email,
                    request
            );

            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        AccountUnlockOtp data = accountUnlockRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found","NOT_FOUND", HttpStatus.NOT_FOUND));

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    email,
                    AuditStatus.FAILED,
                    "Expired unlock OTP used",
                    request
            );
            accountUnlockRepository.deleteById(email);
            throw new org.springframework.security.authentication.CredentialsExpiredException("OTP expired");
        }

        if (data.getUnlockAttempts() >= 3) {
            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    email,
                    AuditStatus.FAILED,
                    "Too many invalid OTP attempts",
                    request
            );
            accountUnlockRepository.deleteById(email);
            throw new org.springframework.security.authentication.LockedException("Too many OTP attempts");
        }

        if (!passwordEncoder.matches(otpInput, data.getOtpHash())) {
            int attempts = data.getUnlockAttempts() + 1;
            data.setUnlockAttempts(attempts);
            accountUnlockRepository.save(data);
            System.out.println("Failed OTP attempt: " + attempts + " for " + email);
            auditLogService.log(
                    user,
                    Action.VERIFY_OTP,
                    "AUTH",
                    email,
                    AuditStatus.FAILED,
                    "Invalid unlock OTP entered",
                    request
            );
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid OTP. Attempt " + attempts + "/3"
            );
        }


                        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
        auditLogService.log(
                user,
                Action.ACCOUNT_UNLOCK,
                "AUTH",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Account unlocked successfully",
                request
        );

        accountUnlockRepository.deleteById(email);

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = authService.createRefreshToken(user,request);

        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }
}