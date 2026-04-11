package com.guts.Guts_IAM.auth.controller;

import com.guts.Guts_IAM.auth.service.UnlockAccountService;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unlock")
@RequiredArgsConstructor
public class AccountUnlockController {

    private final UnlockAccountService unlockAccountService;

    @PostMapping("/request")
    public String requestOtp(@RequestParam String email) {
        unlockAccountService.sendUnlockOtp(email);
        return "OTP sent";
    }

    @PostMapping("/verify")
    public JwtResponse verify(@RequestParam String email,
                              @RequestParam String otp,
                              HttpServletRequest request) {

        return unlockAccountService.verifyOtpAndUnlock(email, otp, request);
    }
}