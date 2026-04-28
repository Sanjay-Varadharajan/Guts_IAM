package com.guts.Guts_IAM.auth.controller;


import com.guts.Guts_IAM.auth.dto.ForgotPasswordRequest;
import com.guts.Guts_IAM.auth.service.PasswordAuthService;
import com.guts.Guts_IAM.auth.dto.ResetPasswordRequest;
import com.guts.Guts_IAM.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class CredentialsController {

    private final PasswordAuthService passwordAuthService;


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@Valid @RequestBody ForgotPasswordRequest request){
        passwordAuthService.forgotPassword(request);

        ApiResponse<Void> apiResponse=new ApiResponse<>(
                true,
                "OTP_SENT_SUCCESSFULLY",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        passwordAuthService.resetPassword(request);

        ApiResponse<Void> apiResponse=new ApiResponse<>(
                true,
                "PASSWORD_RESET_SUCCESSFULLY",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);



    }
}
