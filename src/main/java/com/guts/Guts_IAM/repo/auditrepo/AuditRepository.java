package com.guts.Guts_IAM.repo.auditrepo;

import com.guts.Guts_IAM.model.audits.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog,Integer> {
}
