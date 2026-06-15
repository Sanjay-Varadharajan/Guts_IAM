package com.guts.Guts_IAM.user.stats;

import com.guts.Guts_IAM.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AdminStatsService {
    private final AdminStatsRepository statsRepo;

    public AdminStatsService(AdminStatsRepository statsRepo) {
        this.statsRepo = statsRepo;
    }

    private AdminStats getStats() {
        return statsRepo.findById(1L)
                .orElseGet(() -> {
                    AdminStats s = new AdminStats();
                    s.setStatsId(1L);
                    return statsRepo.save(s);
                });
    }

    @Transactional
    public void onUserCreated(User user) {
        AdminStats stats = getStats();

        stats.setTotalUsers(stats.getTotalUsers() + 1);

        if (user.isActive()) {
            stats.setTotalActiveUsers(stats.getTotalActiveUsers() + 1);
        } else {
            stats.setTotalInactiveUsers(stats.getTotalInactiveUsers() + 1);
        }

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);

        if (user.getUserCreatedOn().isAfter(startOfDay)) {
            stats.setNewUserToday(stats.getNewUserToday() + 1);
        }
    }

    @Transactional
    public void updateUserStatusStats(boolean oldStatus, boolean newStatus) {

        AdminStats stats = getStats();

        // ACTIVE → INACTIVE
        if (oldStatus && !newStatus) {
            stats.setTotalActiveUsers(stats.getTotalActiveUsers() - 1);
            stats.setTotalInactiveUsers(stats.getTotalInactiveUsers() + 1);
        }

        // INACTIVE → ACTIVE
        else if (!oldStatus && newStatus) {
            stats.setTotalActiveUsers(stats.getTotalActiveUsers() + 1);
            stats.setTotalInactiveUsers(stats.getTotalInactiveUsers() - 1);
        }
    }

    public AdminStats getStatsSnapshot() {
        return getStats();
    }
}
