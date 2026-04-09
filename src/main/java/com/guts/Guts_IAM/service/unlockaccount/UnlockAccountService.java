package com.guts.Guts_IAM.service.unlockaccount;

import com.guts.Guts_IAM.mail.service.EmailService;
import com.guts.Guts_IAM.model.otp.AccountUnlockOtp;
import com.guts.Guts_IAM.model.refreshtoken.RefreshToken;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.otp.AccountUnlockRepo;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.jwt.jwtutils.JwtUtils;
import com.guts.Guts_IAM.security.signup.JwtResponse;
import com.guts.Guts_IAM.service.authservice.AuthService;
import com.guts.Guts_IAM.utils.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UnlockAccountService {

    private final AccountUnlockRepo accountUnlockRepo;
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

        AccountUnlockOtp existing = accountUnlockRepo.findById(email).orElse(null);

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

        accountUnlockRepo.save(data);

        emailService.sendUnlockOtp(email, otp);
    }

    public JwtResponse verifyOtpAndUnlock(String email,
                                          String otpInput,
                                          HttpServletRequest request) {

        AccountUnlockOtp data = accountUnlockRepo.findById(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            accountUnlockRepo.deleteById(email);
            throw new RuntimeException("OTP expired");
        }

        if (data.getUnlockAttempts() >= 3) {
            accountUnlockRepo.deleteById(email);
            throw new RuntimeException("Too many attempts");
        }

        if (!passwordEncoder.matches(otpInput, data.getOtpHash())) {
            int attempts = data.getUnlockAttempts() + 1;
            data.setUnlockAttempts(attempts);
            accountUnlockRepo.save(data);
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

        accountUnlockRepo.deleteById(email);

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = authService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }
}