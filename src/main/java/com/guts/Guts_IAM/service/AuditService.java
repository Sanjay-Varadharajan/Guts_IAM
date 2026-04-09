package com.guts.Guts_IAM.service;


import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAudit(AuditLog auditLog) {
        auditRepo.save(auditLog);
    }
}
