package com.guts.Guts_IAM.controller.unlockcontroller;

import com.guts.Guts_IAM.security.signup.JwtResponse;
import com.guts.Guts_IAM.service.unlockaccount.UnlockAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unlock")
@RequiredArgsConstructor
public class UnlockController {

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