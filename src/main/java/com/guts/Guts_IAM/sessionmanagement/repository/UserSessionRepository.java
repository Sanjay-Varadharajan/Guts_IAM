package com.guts.Guts_IAM.sessionmanagement.repository;

import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession,Long> {

    Optional<UserSession> findByJwtToken(String jwtToken);

    List<UserSession> findByUserUserIdAndRevokedFalse(Integer userId);




    UserSession findTopByUserUserMailOrderByLoginTimeDesc(String userMail);

    long countByUserAndRevokedFalseAndExpiresAtAfter(
            User user,
            LocalDateTime now
    );

}
