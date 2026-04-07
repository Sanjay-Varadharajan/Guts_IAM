package com.guts.Guts_IAM.repo.otp;

import com.guts.Guts_IAM.model.otp.AccountUnlockOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUnlockRepo extends JpaRepository<AccountUnlockOtp,String> {
}
