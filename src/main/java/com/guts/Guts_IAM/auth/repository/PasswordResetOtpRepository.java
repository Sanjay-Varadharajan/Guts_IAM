package com.guts.Guts_IAM.auth.repository;

import com.guts.Guts_IAM.auth.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, String> {

    void deleteByExpiryTimeBefore(LocalDateTime now);
}
