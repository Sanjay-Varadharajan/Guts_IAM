package com.guts.Guts_IAM.auth.controller;

import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.auth.service.SignupService;
import com.guts.Guts_IAM.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(
            @Valid @RequestBody SignupRequest signUpRequest,
            HttpServletRequest httpServletRequest
    ) {

        ApiResponse response =
                signupService.signup(
                        signUpRequest,
                        httpServletRequest
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(
            @RequestParam String token,
            HttpServletRequest httpServletRequest
    ) {

        ApiResponse response =
                signupService.verifyEmail(
                        token,
                        httpServletRequest
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}