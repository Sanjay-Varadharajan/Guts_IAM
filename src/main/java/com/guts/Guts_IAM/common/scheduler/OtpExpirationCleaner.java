package com.guts.Guts_IAM.common.scheduler;

import com.guts.Guts_IAM.auth.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class OtpExpirationCleaner {

    private final PasswordResetOtpRepository otpRepository;


    @Scheduled(fixedRate = 600000) // every 10 min
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }

}
