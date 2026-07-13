package com.guts.Guts_IAM.passwordtracking;

import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.user.model.User;
import lombok.AllArgsConstructor;
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
