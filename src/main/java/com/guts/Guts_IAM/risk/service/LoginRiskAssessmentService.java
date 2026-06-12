package com.guts.Guts_IAM.risk.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.risk.model.LoginRiskAssessment;
import com.guts.Guts_IAM.risk.repository.LoginRiskAssessmentRepository;
import com.guts.Guts_IAM.risk.result.RiskAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginRiskAssessmentService {

    private final LoginRiskAssessmentRepository repository;

    public void saveAssessment(
            AuditLog auditLog,
            RiskAnalysisResult result
    ) {

        LoginRiskAssessment assessment =
                new LoginRiskAssessment();

        assessment.setAuditLog(auditLog);

        assessment.setRiskScore(
                result.getRiskScore()
        );

        assessment.setRiskLevel(
                result.getRiskLevel()
        );

        assessment.setReason(
                String.join(", ", result.getReason())
        );

        assessment.setAssessedAt(
                    LocalDateTime.now()
        );

        repository.save(assessment);
    }
}