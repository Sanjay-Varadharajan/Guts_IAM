package com.guts.Guts_IAM.auth.controller;


import com.guts.Guts_IAM.service.AuthService;
import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.token.refreshtoken.dto.TokenRefreshRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(authService.login(loginRequest,httpServletRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        authService.logout(request.getRefreshToken(),httpServletRequest);
        return ResponseEntity.ok("Logged out successfully");
    }



}
