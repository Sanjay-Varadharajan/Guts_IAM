package com.guts.Guts_IAM.auth.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetOtp {


    @Id
    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private int attempts;

    private LocalDateTime lastRequestedAt;
}
