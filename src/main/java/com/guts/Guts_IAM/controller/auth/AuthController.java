package com.guts.Guts_IAM.controller.auth;


import com.guts.Guts_IAM.security.signup.LoginDto;
import com.guts.Guts_IAM.security.signup.TokenRefreshRequest;
import com.guts.Guts_IAM.service.authservice.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(authService.login(loginDto,httpServletRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request,HttpServletRequest httpServletRequest) {
        authService.logout(request.getRefreshToken(),httpServletRequest);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request,HttpServletRequest httpServletRequest) {

        return ResponseEntity.ok(authService.refreshAccessToken(request.getRefreshToken(),httpServletRequest));
    }


}
