package com.guts.Guts_IAM.auditlog.dto;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDtoForUser {

    private Integer logId;

    private Action logAction;

    private String resource;

    private AuditStatus status;

    private String message;

    private LocalDateTime auditedOn;

    private String ipAddress;

    public AuditLogDtoForUser(AuditLog auditLog) {
        this.logId = auditLog.getLogId();
        this.logAction = auditLog.getLogAction();
        this.resource = auditLog.getResource();
        this.status = auditLog.getStatus();
        this.message = auditLog.getMessage();
        this.auditedOn = auditLog.getAuditedOn();
        this.ipAddress = auditLog.getIpAddress();
    }
}