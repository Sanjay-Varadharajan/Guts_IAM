package com.guts.Guts_IAM.auth.dto;


import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank(message = "Email cannot be empty")
                                     String eMail) {

}

