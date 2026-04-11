package com.guts.Guts_IAM.user.controller.user;


import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
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
