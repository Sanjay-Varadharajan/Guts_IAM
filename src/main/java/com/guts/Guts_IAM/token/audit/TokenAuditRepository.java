package com.guts.Guts_IAM.token.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenAuditRepository extends JpaRepository<TokenAudit,Integer> {
}
