package com.guts.Guts_IAM.repo.refreshtoken;

import com.guts.Guts_IAM.model.RefreshToken;
import com.guts.Guts_IAM.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Ref;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByUser(User user);

}
