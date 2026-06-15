package com.guts.Guts_IAM.user.stats;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "admin_stats")
public class AdminStats {

    @Id
    private Long statsId = 1L;

    private long totalUsers;

    private long totalActiveUsers;

    private long totalInactiveUsers;

    private long newUserToday;
}
