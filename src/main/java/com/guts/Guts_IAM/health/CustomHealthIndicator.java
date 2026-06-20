package com.guts.Guts_IAM.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {


    private final JdbcTemplate jdbcTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        boolean dbStatus = checkDatabase();
        boolean cacheStatus = checkCache();

        if (dbStatus && cacheStatus) {
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("cache", "UP")
                    .withDetail("time", LocalDateTime.now())
                    .build();
        }

        return Health.down()
                .withDetail("database", dbStatus ? "UP" : "DOWN")
                .withDetail("cache", cacheStatus ? "UP" : "DOWN")
                .withDetail("time", LocalDateTime.now())
                .build();
    }

    private boolean checkDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkCache() {
        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
