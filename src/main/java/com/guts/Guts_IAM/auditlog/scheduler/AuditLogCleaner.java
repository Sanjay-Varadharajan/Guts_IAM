package com.guts.Guts_IAM.auditlog.scheduler;


import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditLogCleaner {

    private final AuditRepository auditRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteOldLogs(){
        LocalDateTime cutOff=LocalDateTime.now().minusDays(90);
        int deleted=auditRepository.deleteByAuditedOnBefore(cutOff);
        System.out.println("Deleted " + deleted + " old audit logs");
    }
}
