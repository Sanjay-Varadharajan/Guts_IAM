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
public class AuditLogDto {

    private Integer logId;

    private Action logAction;

    private Integer userId;

    private String userMail;

    private String roleName;

    private String resource;

    private String resourceId;

    private String ipAddress;

    private AuditStatus status;

    private String location;

    private Double latitude;

    private Double longitude;

    private String userAgent;

    private String message;

    private LocalDateTime auditedOn;

    public AuditLogDto(AuditLog auditLog) {
        this.logId = auditLog.getLogId();
        this.logAction = auditLog.getLogAction();
        this.userId = auditLog.getUserId();
        this.userMail = auditLog.getUserMail();
        this.roleName = auditLog.getRoleName();
        this.resource = auditLog.getResource();
        this.resourceId = auditLog.getResourceId();
        this.ipAddress = auditLog.getIpAddress();
        this.status = auditLog.getStatus();
        this.location = auditLog.getLocation();
        this.latitude = auditLog.getLatitude();
        this.longitude = auditLog.getLongitude();
        this.userAgent = auditLog.getUserAgent();
        this.message = auditLog.getMessage();
        this.auditedOn = auditLog.getAuditedOn();
    }
}