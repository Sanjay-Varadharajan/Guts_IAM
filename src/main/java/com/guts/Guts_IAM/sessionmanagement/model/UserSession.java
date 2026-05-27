package com.guts.Guts_IAM.sessionmanagement.model;


import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jwtToken;

    private String device;

    private String browser;

    private String ipAddress;

    private LocalDateTime loginTime;

    private LocalDateTime lastActive;

    private LocalDateTime expiresAt;

    private boolean revoked;

    @ManyToOne
    private User user;
}
