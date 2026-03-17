package com.guts.Guts_IAM.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.guts.Guts_IAM.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Set;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer userId;

    @NotEmpty(message = "name cannot be empty")
    private String userName;

    @NotEmpty(message = "this action requires mail")
    @Email
    @Column(nullable = false,unique = true)
    private String userMail;

    @NotEmpty(message = "password is required")
    @JsonIgnore
    private String userPassword;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Roles> userRoles;

    private boolean active=true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime userCreatedOn;
}
