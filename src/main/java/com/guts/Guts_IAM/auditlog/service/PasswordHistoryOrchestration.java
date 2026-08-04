package com.guts.Guts_IAM.auditlog.service;


import com.guts.Guts_IAM.auditlog.model.PasswordHistory;
import com.guts.Guts_IAM.auditlog.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordHistoryOrchestration {

    private final PasswordHistoryRepository passwordHistoryRepository;

    public void addHistory(Integer userId,String hashedPassword){

        PasswordHistory passwordHistory=new PasswordHistory();
        passwordHistory.setHashedPassword(hashedPassword);
        passwordHistory.setUserId(userId);
        passwordHistoryRepository.save(passwordHistory);
    }
}
