package com.guts.Guts_IAM.model.otp;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "unlock_otp")
public class AccountUnlockOtp {

    @Id
    private String eMail;

    private String otpHash;

    private LocalDateTime expiryTime;

    private int unlockAttempts;

    private LocalDateTime lastRequestedAt;
}
