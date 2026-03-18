package com.guts.Guts_IAM.service.adminservice;


import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.exceptionhandling.exceptions.ForbiddenException;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {



    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> getAllActiveUsers(Principal principal, Pageable pageable) {
        User loggedInUser=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(()->
                new UsernameNotFoundException("User with this mail "+principal.getName()+" does not exist"));


        if (!loggedInUser.getUserRoles().equals(Roles.ROLE_ADMIN)){
            throw new ForbiddenException("Permit Restricted","ACTION_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        Set<String> allowedSort=Set.of("userCreatedOn","userMail");

        pageable.getSort().forEach(order ->
        {
            if(!allowedSort.contains(order.getProperty())){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort field: "+order.getProperty()
                );
            }
        });
        Page<User> activeUsers=userRepository.findAll(pageable);

        return activeUsers;
    }
}
