package com.guts.Guts_IAM.auditlog.model;

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
}
