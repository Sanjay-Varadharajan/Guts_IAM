package com.guts.Guts_IAM.role.repository;

import com.guts.Guts_IAM.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Integer> {

    Optional<Role> findByName(String name);

    boolean existsByName(String roleName);
}

