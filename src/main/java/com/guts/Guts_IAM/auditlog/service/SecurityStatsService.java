package com.guts.Guts_IAM.auditlog.service;


import com.guts.Guts_IAM.apikey.ApiKey;
import com.guts.Guts_IAM.apikey.ApikeyRepository;
import com.guts.Guts_IAM.apikey.Status;
import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.SecurityStats;
import com.guts.Guts_IAM.auth.service.AuthService;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.passwordtracking.PasswordTracker;
import com.guts.Guts_IAM.passwordtracking.PasswordTrackerRepository;
import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.sessionmanagement.repository.UserSessionRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityStatsService {


    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final UserSessionRepository userSessionRepository;
    private final ApikeyRepository apikeyRepository;
    private final PasswordTrackerRepository passwordTrackerRepository;

    public SecurityStats getStats(Authentication authentication, HttpServletRequest httpServletRequest,String userMail){
        Optional<User> user = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (user.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.GET_API_KEY_CREATED_ON_STATS,
                    "API_KEY",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "authenticated user not found",
                    httpServletRequest
            );
            throw new UserNameNotFoundException(
                    "user not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }
        SecurityStats stats=new SecurityStats();

        User user1=userRepository.findByUserMailAndActiveTrue(userMail).orElseThrow(
                ()->new UserNameNotFoundException(
                "user not found",
        "NOT_FOUND",
        HttpStatus.NOT_FOUND
        ));

        UserSession session =
                userSessionRepository.findTopByUserUserMailOrderByLoginTimeDesc(user1.getUserMail());

        long activeSession=userSessionRepository.countByUserAndRevokedFalseAndExpiresAtAfter(user1, LocalDateTime.now());
        long accountAge= ChronoUnit.DAYS.between(
                user1.getUserCreatedOn(),
                LocalDateTime.now()
        );
        long totalApiKeys=apikeyRepository.countByUserId(user1.getUserId());
        PasswordTracker lastPasswordChanged=passwordTrackerRepository.findTopByUserUserIdOrderByPasswordChangedAtDesc(user1.getUserId());

        stats.setLastLogin(session.getLoginTime());
        stats.setActiveSessions(activeSession);
        stats.setAccountAgeDays(accountAge);
        stats.setTotalApiKeys(totalApiKeys);
        stats.setFailedLoginAttempts(user1.getFailedAttempts());
        stats.setLastPasswordChange(lastPasswordChanged.getPasswordChangedAt());

        return stats;


    }
}
