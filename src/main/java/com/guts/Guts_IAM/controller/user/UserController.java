package com.guts.Guts_IAM.controller.user;


import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.security.signup.AuditLogDto;
import com.guts.Guts_IAM.security.signup.UserRequestDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import com.guts.Guts_IAM.service.userservice.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> viewProfile(Principal principal){

        UserResponseDto profile=userService.viewProfile(principal);

        ApiResponse response=new ApiResponse<>(
                true,
                principal.getName()+" PROFILE_FETCHED",
                profile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping("/me/update")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(@RequestBody UserRequestDto userRequestDto, Principal principal){

        UserResponseDto updatedProfile=userService.updateProfile(userRequestDto,principal);

        ApiResponse apiResponse=new ApiResponse(
                true,
                "PROFILE_UPDATED",
                updatedProfile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
