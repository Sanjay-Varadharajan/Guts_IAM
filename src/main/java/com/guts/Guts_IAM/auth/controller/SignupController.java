package com.guts.Guts_IAM.auth.controller;

import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.auth.service.SignupService;
import com.guts.Guts_IAM.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupService signupService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest httpServletRequest){
        SignupRequest response=signupService.signup(signUpRequest,httpServletRequest);

        ApiResponse apiResponse=new ApiResponse(
                true,
                "Signed Up Successfully",
                response,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
