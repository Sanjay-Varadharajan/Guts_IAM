package com.guts.Guts_IAM.repo.refreshtokenrepo;

import com.guts.Guts_IAM.model.refreshtoken.RefreshToken;
import com.guts.Guts_IAM.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByUser(User user);

}
