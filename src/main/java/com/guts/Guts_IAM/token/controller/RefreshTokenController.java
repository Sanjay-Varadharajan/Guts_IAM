package com.guts.Guts_IAM.token.controller;

import com.guts.Guts_IAM.token.refreshtoken.dto.TokenRefreshRequest;
import com.guts.Guts_IAM.token.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.getRefreshToken(),httpServletRequest));
    }

}
