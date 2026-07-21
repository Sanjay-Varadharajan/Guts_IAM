package com.guts.Guts_IAM.passwordtracking.model;

import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackerId;

    private String changedPasswordHash;

    private LocalDateTime passwordChangedAt;

    private long totalChanges;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


}
