package com.guts.Guts_IAM.risk.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.geolocation.utils.GeoUtils;
import com.guts.Guts_IAM.risk.level.RiskLevel;
import com.guts.Guts_IAM.risk.result.RiskAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RiskAnalysisServiceImpl
        implements RiskAnalysisService {

    private final AuditRepository auditRepository;



    @Override
    public RiskAnalysisResult analyze(
            AuditLog currentLogin
    ) {

        int riskScore = 0;

        List<String> reasons = new ArrayList<>();

        List<AuditLog> previousLogins =
                auditRepository
                        .findTop10ByUserIdAndLogActionAndStatusOrderByAuditedOnDesc(
                                currentLogin.getUserId(),
                                Action.LOGIN,
                                AuditStatus.SUCCESS
                        );

        System.out.println("===== CURRENT LOGIN =====");
        System.out.println("IP = " + currentLogin.getIpAddress());
        System.out.println("UA = " + currentLogin.getUserAgent());
        System.out.println("LOCATION = " + currentLogin.getLocation());

        System.out.println("===== PREVIOUS LOGINS =====");

        previousLogins.forEach(log -> {
            System.out.println(
                    "ID=" + log.getLogId()
                            + " ACTION=" + log.getLogAction()
                            + " IP=" + log.getIpAddress()
                            + " UA=" + log.getUserAgent()
                            + " LOCATION=" + log.getLocation()
            );
        });

        if(previousLogins.isEmpty()) {
            return RiskAnalysisResult.builder()
                    .riskScore(0)
                    .riskLevel(RiskLevel.LOW)
                    .reason(Collections.singletonList("First Login"))
                    .build();
        }

        boolean knownIp =
                previousLogins.stream()
                        .anyMatch(log ->
                                Objects.equals(
                                        currentLogin.getIpAddress(),
                                        log.getIpAddress()
                                ));

        if(!knownIp) {
            riskScore += 20;
            reasons.add("New IP");
        }



        boolean knownDevice =
                previousLogins.stream()
                        .anyMatch(log ->
                                Objects.equals(
                                        currentLogin.getUserAgent(),
                                        log.getUserAgent()
                                ));

        if(!knownDevice) {
            riskScore += 30;
            reasons.add("New Device");
        }

        boolean knownLocation =
                previousLogins.stream()
                        .anyMatch(log ->
                                Objects.equals(
                                        currentLogin.getLocation(),
                                        log.getLocation()
                                ));

        if(!knownLocation) {
            riskScore += 30;
            reasons.add("New Location");
        }
        System.out.println("Known IP = " + knownIp);
        System.out.println("Known Device = " + knownDevice);
        System.out.println("Known Location = " + knownLocation);

        AuditLog lastLogin =
                previousLogins.get(0);

        if(lastLogin.getLatitude() != null
                && lastLogin.getLongitude() != null
                && currentLogin.getLatitude() != null
                && currentLogin.getLongitude() != null
                && lastLogin.getAuditedOn() != null){

            double distance =
                    GeoUtils.distance(
                            lastLogin.getLatitude(),
                            lastLogin.getLongitude(),
                            currentLogin.getLatitude(),
                            currentLogin.getLongitude()
                    );

            long minutes =
                    Duration.between(
                            lastLogin.getAuditedOn(),
                            LocalDateTime.now()
                    ).toMinutes();

            if(distance > 1000
                    && minutes < 120) {

                riskScore += 80;

                reasons.add(
                        "Impossible Travel"
                );
            }
        }

        RiskLevel level;

        if(riskScore >= 80) {
            level = RiskLevel.HIGH;
        }
        else if(riskScore >= 40) {
            level = RiskLevel.MEDIUM;
        }
        else {
            level = RiskLevel.LOW;
        }

        if (reasons.isEmpty()) {

            reasons.add(
                    "Known Login Pattern"
            );
        }

        return RiskAnalysisResult.builder()
                .riskScore(riskScore)
                .riskLevel(level)
                .reason(reasons)
                .build();
    }
}