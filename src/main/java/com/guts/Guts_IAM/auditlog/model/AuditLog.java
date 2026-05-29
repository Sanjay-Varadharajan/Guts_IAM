package com.guts.Guts_IAM.auditlog.model;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @Enumerated(EnumType.STRING)
    private Action logAction;

    private Integer userId;

    private String userMail;

    private String roleName;

    private String resource;

    private String resourceId;

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    private String location;

    private Double latitude;

    private Double longitude;

    private String userAgent;

    private String message;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime auditedOn;
}
