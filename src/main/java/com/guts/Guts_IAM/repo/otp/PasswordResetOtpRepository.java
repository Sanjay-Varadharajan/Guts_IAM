package com.guts.Guts_IAM.repo.otp;

import com.guts.Guts_IAM.model.otp.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, String> {

    void deleteByExpiryTimeBefore(LocalDateTime now);
}
