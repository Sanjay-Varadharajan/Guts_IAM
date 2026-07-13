package com.guts.Guts_IAM.passwordtracking;

import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrackDto {

    private Long trackerId;

    private LocalDateTime passwordChangedAt;

    private long totalChanges;

    private int userId;
}
