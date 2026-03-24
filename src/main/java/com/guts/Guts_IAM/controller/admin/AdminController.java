package com.guts.Guts_IAM.controller.admin;


import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.security.signup.AuditLogDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import com.guts.Guts_IAM.service.adminservice.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final AdminService adminService;

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllActiveUsers(Principal principal, @PageableDefault(
            page = 0,
            size = 10,
            sort = "userCreatedOn",
            direction = Sort.Direction.DESC)
                                        Pageable pageable,HttpServletRequest httpServletRequest
    )
    {

        Page<UserResponseDto> response=adminService.getAllActiveUsers(principal,pageable);

        ApiResponse apiResponse=new ApiResponse<>(
                true,
                "Active Users",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PatchMapping("/user/{userId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(@PathVariable Integer userId, Principal principal, HttpServletRequest httpServletRequest){
        UserResponseDto response=adminService.updateUserStatus(userId,principal);

        ApiResponse apiResponse=new ApiResponse<>(
                true,
                "",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/logs/viewall")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAllAuditLog(Principal principal,
                                                                         @PageableDefault(
                                                                              page = 0,
                                                                              size = 10,
                                                                              sort = "auditedOn",
                                                                              direction = Sort.Direction.DESC)
                                                                      Pageable pageable
                                                                      ){

        Page<AuditLogDto> dtoResponse=adminService.getAllAuditLog(principal,pageable);

        ApiResponse response=new ApiResponse<>(
                true,
                "AUDIT_LOG_FETCHED",
                dtoResponse,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
