package com.guts.Guts_IAM.controller.unlockcontroller;

import com.guts.Guts_IAM.service.unlockaccount.UnlockAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String verify(@RequestParam String email,
                         @RequestParam String otp) {
        unlockAccountService.verifyAndUnlock(email, otp);
        return "Account unlocked";
    }




}
