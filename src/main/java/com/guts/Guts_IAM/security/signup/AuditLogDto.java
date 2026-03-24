package com.guts.Guts_IAM.security.signup;

import com.guts.Guts_IAM.model.audits.AuditLog;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {

    private Integer logId;

    private String logAction;

    private Integer userId;

    private String userMail;

    private String roleName;

    private String resource;

    private String resourceId;

    private String ipAddress;

    private String userAgent;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime auditedOn;

    public AuditLogDto(AuditLog auditLog) {
        this.auditedOn=auditLog.getAuditedOn();
        this.ipAddress=auditLog.getIpAddress();
        this.logAction=auditLog.getLogAction();
        this.logId=auditLog.getLogId();
        this.resource=auditLog.getResource();
        this.resourceId=auditLog.getResourceId();
        this.roleName=auditLog.getRoleName();
        this.userAgent=auditLog.getUserAgent();
        this.userId=auditLog.getUserId();
    }
}
