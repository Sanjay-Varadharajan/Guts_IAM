package com.guts.Guts_IAM.repo.auditrepo;

import com.guts.Guts_IAM.model.tokenaudit.TokenAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenAuditRepository extends JpaRepository<TokenAudit,Integer> {
}
