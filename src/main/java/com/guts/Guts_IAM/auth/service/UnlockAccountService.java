package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auth.model.AccountUnlockOtp;
import com.guts.Guts_IAM.auth.repository.AccountUnlockRepository;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.common.util.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UnlockAccountService {

    private final AccountUnlockRepository accountUnlockRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    private static final int OTP_LENGTH = 6;

    public void sendUnlockOtp(String email) {

        User user = userRepository.findByUserMailAndActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isAccountNonLocked()) {
            throw new RuntimeException("Account is not locked");
        }

        AccountUnlockOtp existing = accountUnlockRepository.findById(email).orElse(null);

        if (existing != null &&
                existing.getLastRequestedAt() != null &&
                existing.getLastRequestedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Wait before requesting again");
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
    }

    public JwtResponse verifyOtpAndUnlock(String email,
                                          String otpInput,
                                          HttpServletRequest request) {

        AccountUnlockOtp data = accountUnlockRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            accountUnlockRepository.deleteById(email);
            throw new RuntimeException("OTP expired");
        }

        if (data.getUnlockAttempts() >= 3) {
            accountUnlockRepository.deleteById(email);
            throw new RuntimeException("Too many attempts");
        }

        if (!passwordEncoder.matches(otpInput, data.getOtpHash())) {
            int attempts = data.getUnlockAttempts() + 1;
            data.setUnlockAttempts(attempts);
            accountUnlockRepository.save(data);
            System.out.println("Failed OTP attempt: " + attempts + " for " + email); // <- debug
            throw new RuntimeException("Invalid OTP. Attempt " + attempts + "/3");
        }

        User user = userRepository.findByUserMailAndActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        accountUnlockRepository.deleteById(email);

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = authService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }
}