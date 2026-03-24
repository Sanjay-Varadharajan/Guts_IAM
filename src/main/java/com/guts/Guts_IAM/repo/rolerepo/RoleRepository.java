package com.guts.Guts_IAM.repo.rolerepo;

import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Integer> {

    Optional<Role> findByName(Roles name);
}
