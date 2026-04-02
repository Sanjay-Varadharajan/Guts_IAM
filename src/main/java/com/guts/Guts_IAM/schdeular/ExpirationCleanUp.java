package com.guts.Guts_IAM.schdeular;

import com.guts.Guts_IAM.repo.otp.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ExpirationCleanUp {

    private final PasswordResetOtpRepository otpRepository;


    @Scheduled(fixedRate = 600000) // every 10 min
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
