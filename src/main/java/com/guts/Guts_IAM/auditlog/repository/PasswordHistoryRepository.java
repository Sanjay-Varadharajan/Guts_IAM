package com.guts.Guts_IAM.auditlog.repository;

import com.guts.Guts_IAM.auditlog.model.PasswordHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory,Integer> {


    Page<PasswordHistory> findByUserId(Integer userId, Pageable pageable);
}
