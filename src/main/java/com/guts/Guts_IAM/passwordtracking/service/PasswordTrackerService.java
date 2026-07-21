package com.guts.Guts_IAM.passwordtracking.service;

import com.guts.Guts_IAM.passwordtracking.repo.PasswordTrackerRepository;
import com.guts.Guts_IAM.passwordtracking.model.PasswordTracker;
import com.guts.Guts_IAM.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordTrackerService{

    private final PasswordTrackerRepository passwordTrackerRepository;

    public void trackChange(String password, User user){
        PasswordTracker passwordTracker=new PasswordTracker();
        passwordTracker.setChangedPasswordHash(password);
        passwordTracker.setUser(user);
        passwordTracker.setTotalChanges(passwordTracker.getTotalChanges()+1);
        passwordTracker.setPasswordChangedAt(LocalDateTime.now());
        passwordTrackerRepository.save(passwordTracker);
    }
}
