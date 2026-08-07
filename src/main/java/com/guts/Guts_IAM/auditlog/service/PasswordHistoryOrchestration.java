package com.guts.Guts_IAM.auditlog.service;


import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.PasswordHistory;
import com.guts.Guts_IAM.auditlog.repository.PasswordHistoryRepository;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordHistoryOrchestration {

    private final PasswordHistoryRepository passwordHistoryRepository;

    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    public void addHistory(Integer userId,String hashedPassword){
        PasswordHistory passwordHistory=new PasswordHistory();
        passwordHistory.setHashedPassword(hashedPassword);
        passwordHistory.setUserId(userId);
        passwordHistoryRepository.save(passwordHistory);
    }

    public Page<PasswordHistory> viewPasswordHistory(Authentication authentication, HttpServletRequest httpServletRequest, Pageable pageable) {

        Optional<User> user=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (user.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.VIEW_PASSWORD_HISTORY,
                    "PASSWORD",
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
        User requestingUser=user.get();
        Page<PasswordHistory> passwordHistories=passwordHistoryRepository.findByUserId(requestingUser.getUserId(),pageable);
        auditLogService.log(requestingUser,
                Action.VIEW_PASSWORD_HISTORY,
                "PASSWORD",
                requestingUser.getUserId().toString(),
                AuditStatus.SUCCESS,
                "password history fetched",
                httpServletRequest);

        return passwordHistories;
    }
}
