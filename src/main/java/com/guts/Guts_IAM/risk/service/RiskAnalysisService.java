package com.guts.Guts_IAM.risk.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.risk.result.RiskAnalysisResult;

public interface RiskAnalysisService {
    RiskAnalysisResult analyze(
           AuditLog currentLogin
    );
}
