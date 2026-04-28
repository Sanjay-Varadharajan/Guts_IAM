package com.guts.Guts_IAM.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @JsonProperty("eMail")
        @NotBlank(message = "Email cannot be empty")
            String email,
        @NotBlank(message = "OTP cannot be empty")
        String otp,
        @NotBlank(message = "Password cannot be empty")
        String newPassword
    ) {}
