package com.guts.Guts_IAM.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
public class TokenAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenAuditId;

    @NotEmpty(message = "Generated access_token cannot be empty")
    private String accessToken;

    @Email
    @NotEmpty(message = "token owner cannot be empty")
    private String tokenOwner;

    @NotEmpty(message = "action is required for auditing")
    private String action;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime tokenGeneratedOn;
}
