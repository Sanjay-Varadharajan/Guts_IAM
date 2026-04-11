package com.guts.Guts_IAM.auditlog.repository;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditRepository extends JpaRepository<AuditLog,Integer> {


    Page<AuditLog> findByUserMail(Pageable pageable,String userMail);

    int deleteByAuditedOnBefore(LocalDateTime cutoff);

}
