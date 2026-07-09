package com.guts.Guts_IAM.passwordtracking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordTrackerRepository extends JpaRepository<PasswordTracker,Integer> {
}
