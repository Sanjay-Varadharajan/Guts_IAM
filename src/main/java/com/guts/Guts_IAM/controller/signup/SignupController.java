package com.guts.Guts_IAM.controller.signup;

import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.security.signup.SignUpDto;
import com.guts.Guts_IAM.service.signupservice.SignupService;
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
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignUpDto signUpDto){
        SignUpDto response=signupService.signup(signUpDto);

        ApiResponse apiResponse=new ApiResponse(
                true,
                "Signed Up Successfully",
                response,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
