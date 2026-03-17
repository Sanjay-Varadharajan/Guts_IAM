package com.guts.Guts_IAM.repo.user;

import com.guts.Guts_IAM.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUserMailAndActiveTrue(String userMail);

}
