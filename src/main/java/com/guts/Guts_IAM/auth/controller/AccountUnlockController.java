package com.guts.Guts_IAM.auth.controller;

import com.guts.Guts_IAM.auth.service.UnlockAccountService;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unlock")
@RequiredArgsConstructor
public class AccountUnlockController {

    private final UnlockAccountService unlockAccountService;

    @PostMapping("/request")
    public ResponseEntity<String> requestOtp(@RequestParam String email,HttpServletRequest httpServletRequest) {
        unlockAccountService.sendUnlockOtp(email,httpServletRequest);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .body("OTP sent");
    }

    @PostMapping("/verify")
    public ResponseEntity<JwtResponse> verify(@RequestParam String email,
                              @RequestParam String otp,
                              HttpServletRequest request) {

        JwtResponse response=unlockAccountService.verifyOtpAndUnlock(email, otp, request);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}