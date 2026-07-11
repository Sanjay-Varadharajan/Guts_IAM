package com.guts.Guts_IAM.passwordtracking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordTrackerRepository extends JpaRepository<PasswordTracker,Integer> {
    Page<PasswordTracker> findByUserUserId(long userId,Pageable pageable);
}

