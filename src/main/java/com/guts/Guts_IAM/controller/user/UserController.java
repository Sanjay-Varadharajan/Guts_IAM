package com.guts.Guts_IAM.controller.user;


import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.security.signup.AuditLogDto;
import com.guts.Guts_IAM.security.signup.AuditLogDtoForUser;
import com.guts.Guts_IAM.security.signup.UserRequestDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import com.guts.Guts_IAM.service.userservice.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<ApiResponse<UserResponseDto>> viewProfile(Principal principal, HttpServletRequest request){

        UserResponseDto profile=userService.viewProfile(principal,request);

        ApiResponse response=new ApiResponse<>(
                true,
                principal.getName()+" PROFILE_FETCHED",
                profile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping("/me/update")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(@RequestBody UserRequestDto userRequestDto, Principal principal,HttpServletRequest request){

        UserResponseDto updatedProfile=userService.updateProfile(userRequestDto,principal,request);

        ApiResponse apiResponse=new ApiResponse(
                true,
                "PROFILE_UPDATED",
                updatedProfile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/me/logs")
    public ResponseEntity<ApiResponse<Page<AuditLogDtoForUser>>> viewLogs(Principal principal,
                                                                          @PageableDefault(
                                                                                  page = 0,
                                                                          size= 10,
                                                                          sort ="auditedOn",
                                                                          direction = Sort.Direction.DESC)
                                                                          Pageable pageable
    ,HttpServletRequest request){

        Page<AuditLogDtoForUser> auditResponse=userService.viewLogs(principal,pageable,request);

        ApiResponse response=new ApiResponse<>(
                true,
                "USER_AUDIT_LOG",
                auditResponse,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
