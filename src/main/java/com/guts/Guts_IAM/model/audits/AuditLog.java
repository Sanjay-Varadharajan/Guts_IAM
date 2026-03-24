package com.guts.Guts_IAM.model.audits;

import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.model.user.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

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
