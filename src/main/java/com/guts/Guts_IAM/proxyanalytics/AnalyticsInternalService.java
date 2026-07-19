package com.guts.Guts_IAM.proxyanalytics;


import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.ApiKeyNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyticsInternalService {

    private final ProxyClientService proxyClientService;

    private final AuditLogService auditLogService;

    private final UserRepository userRepository;

    public ApiKeyAnalyticsDto proxyAnalytics(String apiKey, Authentication authentication, HttpServletRequest httpServletRequest) {

        Optional<User> user = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (user.isEmpty()) {
            auditLogService.log(
                    null,
                    Action.GET_REQUEST_ANALYTICS,
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
        if(apiKey==null || apiKey.isBlank()){

            auditLogService.log(
                    user.get(),
                    Action.GET_REQUEST_ANALYTICS,
                    "API_KEY",
                    user.get().getUserId().toString(),
                    AuditStatus.FAILED,
                    "api key is empty",
                    httpServletRequest
            );
            throw new ApiKeyNotFoundException("Api key is Empty","NOT_FOUND",HttpStatus.NOT_FOUND);
        }

        auditLogService.log(
                user.get(),
                Action.GET_REQUEST_ANALYTICS,
                "request",
                user.get().getUserId().toString(),
                AuditStatus.SUCCESS,
                "fetched request_analytics",
                httpServletRequest
        );
       return proxyClientService.getAnalytics(apiKey);

    }
}
