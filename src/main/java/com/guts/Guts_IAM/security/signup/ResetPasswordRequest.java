package com.guts.Guts_IAM.security.signup;

public record ResetPasswordRequest(
            String email,
            String otp,
            String newPassword
    ) {}
