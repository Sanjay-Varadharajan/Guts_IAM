package com.guts.Guts_IAM.auth.dto;

public record ResetPasswordRequest(
            String email,
            String otp,
            String newPassword
    ) {}
