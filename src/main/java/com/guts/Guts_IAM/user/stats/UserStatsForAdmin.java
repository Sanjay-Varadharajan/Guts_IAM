package com.guts.Guts_IAM.user.stats;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsForAdmin {

    private long totalUsers;

    private long totalActiveUsers;

    private long totalInactiveUsers;

    private long newUserToday;

    public UserStatsForAdmin(AdminStats stats) {
        this.newUserToday=stats.getNewUserToday();
        this.totalActiveUsers=stats.getTotalActiveUsers();
        this.totalUsers=stats.getTotalUsers();
        this.totalInactiveUsers=stats.getTotalInactiveUsers();
    }
}
