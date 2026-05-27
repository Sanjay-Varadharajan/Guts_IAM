package com.guts.Guts_IAM.sessionmanagement.repository;

import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession,Long> {

    Optional<UserSession> findByJwtToken(String jwtToken);

    List<UserSession> findByUserUserIdAndRevokedFalse(Integer userId);


}
