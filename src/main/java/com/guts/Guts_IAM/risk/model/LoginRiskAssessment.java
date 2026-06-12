package com.guts.Guts_IAM.risk.model;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.risk.level.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "audit_log_id")
    private AuditLog auditLog;


    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(length = 1000)
    private String reason;

    private LocalDateTime assessedAt;
}