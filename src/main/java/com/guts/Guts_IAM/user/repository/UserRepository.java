package com.guts.Guts_IAM.user.repository;

import com.guts.Guts_IAM.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUserMailAndActiveTrue(String userMail);

    Page<User> findByActiveTrue(Pageable pageable);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByUserMail(String name);
}
