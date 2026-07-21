package com.guts.Guts_IAM.passwordtracking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrackDto {

    private Long trackerId;

    private LocalDateTime passwordChangedAt;

    private long totalChanges;

    private long userId;
}
