package com.guts.Guts_IAM.user.controller.admin;


import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.user.dto.admin.AdminRequestDto;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.service.admin.AdminService;
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

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllActiveUsers(Principal principal, @PageableDefault(
            page = 0,
            size = 10,
            sort = "userCreatedOn",
            direction = Sort.Direction.DESC)
                                        Pageable pageable,HttpServletRequest request
    )
    {

        Page<UserResponseDto> response=adminService.getAllActiveUsers(principal,pageable,request);

        ApiResponse apiResponse=new ApiResponse<>(
                true,
                "Active Users",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PatchMapping("/user/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(@PathVariable Integer userId, Principal principal, HttpServletRequest request){
        UserResponseDto response=adminService.updateUserStatus(userId,principal,request);

        ApiResponse apiResponse=new ApiResponse<>(
                true,
                "",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/logs/viewall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAllAuditLog(Principal principal,
                                                                         @PageableDefault(
                                                                              page = 0,
                                                                              size = 10,
                                                                              sort = "auditedOn",
                                                                              direction = Sort.Direction.DESC)
                                                                      Pageable pageable,
                                                                         HttpServletRequest request
                                                                      ){

        Page<AuditLogDto> dtoResponse=adminService.getAllAuditLog(principal,pageable,request);

        ApiResponse response=new ApiResponse<>(
                true,
                "AUDIT_LOG_FETCHED",
                dtoResponse,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> viewProfile(Principal principal, HttpServletRequest request){

        UserResponseDto profile=adminService.viewProfile(principal,request);

        ApiResponse response=new ApiResponse<>(
                true,
                principal.getName()+" PROFILE_FETCHED",
                profile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/me/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(@RequestBody AdminRequestDto adminRequestDto, Principal principal, HttpServletRequest request){

        UserResponseDto updatedProfile=adminService.updateProfile(adminRequestDto,principal,request);

        ApiResponse apiResponse=new ApiResponse(
                true,
                "PROFILE_UPDATED",
                updatedProfile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

}
