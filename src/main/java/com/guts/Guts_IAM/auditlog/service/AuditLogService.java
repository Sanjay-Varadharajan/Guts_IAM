package com.guts.Guts_IAM.auditlog.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.geolocation.service.GeoIPService;
import com.guts.Guts_IAM.risk.result.RiskAnalysisResult;
import com.guts.Guts_IAM.risk.service.LoginRiskAssessmentService;
import com.guts.Guts_IAM.risk.service.RiskAnalysisService;
import com.guts.Guts_IAM.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditRepository auditRepository;
    private final GeoIPService geoIPService;
    private final RiskAnalysisService riskAnalysisService;

    private final LoginRiskAssessmentService
            loginRiskAssessmentService;

    public void log(
            User user,
            Action action,
            String resource,
            String resourceId,
            AuditStatus auditStatus,
            String message,
            HttpServletRequest request
    ) {

        String ip = extractIp(request);

        String userAgent =
                Optional.ofNullable(
                        request.getHeader("User-Agent")
                ).orElse("UNKNOWN");

        saveAuditAsync(
                user,
                action,
                resource,
                resourceId,
                auditStatus,
                message,
                ip,
                userAgent
        );
    }

    @Async
    public void saveAuditAsync(
            User user,
            Action action,
            String resource,
            String resourceId,
            AuditStatus auditStatus,
            String message,
            String ip,
            String userAgent
    ) {

        try {

            var geo = geoIPService.getLocation(ip);

            AuditLog auditLog = new AuditLog();

            if(user!=null) {
                auditLog.setLogAction(action);

                auditLog.setUserId(user.getUserId());

                auditLog.setUserMail(user.getUserMail());

                auditLog.setRoleName(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName())
                                .collect(Collectors.joining(", "))
                );

            }

            auditLog.setResource(resource);

            auditLog.setResourceId(resourceId);

            auditLog.setStatus(auditStatus);

            auditLog.setIpAddress(ip);

            auditLog.setUserAgent(userAgent);

            auditLog.setMessage(message);

            if (geo != null) {

                auditLog.setLocation(
                        geo.getCity() + ", " + geo.getCountry()
                );

                auditLog.setLatitude(
                        geo.getLatitude()
                );

                auditLog.setLongitude(
                        geo.getLongitude()
                );
            }

            RiskAnalysisResult result = null;

            if (action == Action.LOGIN
                    && auditStatus == AuditStatus.SUCCESS
                    && user != null) {

                result = riskAnalysisService.analyze(
                        auditLog
                );
            }

            AuditLog savedAudit =
                    auditRepository.save(auditLog);

            if (result != null) {

                loginRiskAssessmentService
                        .saveAssessment(
                                savedAudit,
                                result
                        );
            }

        } catch (Exception e) {

            log.error("Failed to save audit log", e);
        }
    }

    private String extractIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty()) {

            ip = request.getRemoteAddr();

        } else {

            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}