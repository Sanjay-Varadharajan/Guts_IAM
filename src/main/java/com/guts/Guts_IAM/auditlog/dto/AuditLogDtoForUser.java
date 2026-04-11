package com.guts.Guts_IAM.auditlog.dto;


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

    private String logAction;

    private String resource;

    private LocalDateTime auditedOn;

    private String ipAddress;

    public AuditLogDtoForUser(AuditLog auditLog) {
        this.auditedOn=auditLog.getAuditedOn();
        this.logAction=auditLog.getLogAction();
        this.ipAddress=auditLog.getIpAddress();
        this.logId=auditLog.getLogId();
        this.resource=auditLog.getResource();
    }
}
