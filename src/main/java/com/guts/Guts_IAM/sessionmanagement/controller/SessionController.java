package com.guts.Guts_IAM.sessionmanagement.controller;


import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.sessionmanagement.repository.UserSessionRepository;
import com.guts.Guts_IAM.sessionmanagement.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final UserSessionRepository sessionRepository;

    private final SessionService sessionService;



    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSession>>> getSessions(
            Authentication authentication) {

        List<UserSession> sessions=sessionService.getSessions(authentication);

        ApiResponse<List<UserSession>> apiResponse=new ApiResponse<>(
                true,
                "ACTIVE_SESSIONS",
                sessions,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }
}
