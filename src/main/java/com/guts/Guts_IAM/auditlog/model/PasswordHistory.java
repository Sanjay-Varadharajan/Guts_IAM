package com.guts.Guts_IAM.auditlog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordHistory{

    private Long historyId;

    private Integer userId;

    private String hashedPassword;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime passwordCreatedOn;
}
