package com.guts.Guts_IAM.auditlog.controller;


import com.guts.Guts_IAM.auditlog.model.PasswordHistory;
import com.guts.Guts_IAM.auditlog.service.PasswordHistoryOrchestration;
import com.guts.Guts_IAM.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordHistoryController {

    private final PasswordHistoryOrchestration passwordHistoryOrchestration;



    @GetMapping("/history/get")
    public ResponseEntity<ApiResponse<Page<PasswordHistory>>> viewPasswordHistory(Authentication authentication,
                                                                                  HttpServletRequest httpServletRequest,
                                                                                  @PageableDefault(page = 0,
                                                                                  size = 10,
                                                                                  sort = "passwordCreatedOn",
                                                                                  direction = Sort.Direction.DESC) Pageable pageable){

        Page<PasswordHistory> viewPasswordHistory=passwordHistoryOrchestration.viewPasswordHistory(authentication,httpServletRequest,pageable);

        ApiResponse<Page<PasswordHistory>> apiResponse=new ApiResponse<>(
                true,
                "Password History Fetched",
                viewPasswordHistory,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);





    }
}
