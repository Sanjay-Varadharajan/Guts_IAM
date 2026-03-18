package com.guts.Guts_IAM.controller.admin;


import com.guts.Guts_IAM.exceptionhandling.apiresponse.ApiResponse;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.service.adminservice.AdminService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    @Autowired
    private final AdminService adminService;

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllActiveUsers(Principal principal, @PageableDefault(
            page = 0,
            size = 10,
            sort = "userCreatedOn",
            direction = Sort.Direction.DESC)
                                        Pageable pageable
    )
    {

        Page<User> response=adminService.getAllActiveUsers(principal,pageable);

        ApiResponse apiResponse=new ApiResponse<>(
                true,
                "Active Users",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }


}
