package com.guts.Guts_IAM.auditlog.model;

import com.guts.Guts_IAM.user.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SecurityStats {


    private LocalDateTime lastLogin;
    private LocalDateTime lastPasswordChange;
    private long activeSessions;
    private long totalApiKeys;
    private long failedLoginAttempts;
    private long accountAgeDays;
}
