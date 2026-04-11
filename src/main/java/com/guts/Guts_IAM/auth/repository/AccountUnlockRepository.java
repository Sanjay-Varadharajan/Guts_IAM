package com.guts.Guts_IAM.auth.repository;

import com.guts.Guts_IAM.auth.model.AccountUnlockOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUnlockRepository extends JpaRepository<AccountUnlockOtp,String> {
}
