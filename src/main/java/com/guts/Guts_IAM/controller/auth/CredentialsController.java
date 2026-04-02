package com.guts.Guts_IAM.controller.auth;


import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.security.signup.ForgotPasswordRequest;
import com.guts.Guts_IAM.security.signup.ResetPasswordRequest;
import com.guts.Guts_IAM.service.mailauthSerive.MailAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class CredentialsController {

    private final MailAuthService mailAuthService;


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@Valid @RequestBody ForgotPasswordRequest request){
        mailAuthService.forgotPassword(request);

        ApiResponse<Void> apiResponse=new ApiResponse<>(
                true,
                "OTP_SENT_SUCCESSFULLY",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword( @Valid @RequestBody ResetPasswordRequest request){
        mailAuthService.resetPassword(request);

        ApiResponse<Void> apiResponse=new ApiResponse<>(
                true,
                "PASSWORD_RESET_SUCCESSFULLY",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);



    }
}
