package com.guts.Guts_IAM.sessionmanagement.dto;


import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDto {

    private Long id;

    private String device;

    private String browser;

    private String ipAddress;

    private LocalDateTime loginTime;

    private LocalDateTime lastActive;

    private LocalDateTime expiresAt;

    private boolean revoked;

    private UserResponseDto user;

    public SessionDto(UserSession session) {
        this.id = session.getId();
        this.device = session.getDevice();
        this.browser = session.getBrowser();
        this.ipAddress = session.getIpAddress();
        this.loginTime = session.getLoginTime();
        this.lastActive = session.getLastActive();
        this.expiresAt = session.getExpiresAt();
        this.revoked = session.isRevoked();
        this.user = new UserResponseDto(session.getUser());
    }

}
