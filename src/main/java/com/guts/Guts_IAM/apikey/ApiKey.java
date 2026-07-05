package com.guts.Guts_IAM.apikey;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ApiKey {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apiKeyId;

    private String hashedApiKey;


    private long userId;

    private Status keyStatus=Status.ACTIVE;


    private LocalDateTime lastUsedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime keyCreatedOn;

    private LocalDateTime expiresAt;

}
