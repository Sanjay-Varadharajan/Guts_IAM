package com.guts.Guts_IAM.repo.userrepo;

import com.guts.Guts_IAM.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUserMailAndActiveTrue(String userMail);

    Page<User> findByActiveTrue(Pageable pageable);

}
