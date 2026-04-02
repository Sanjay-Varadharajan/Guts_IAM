package com.guts.Guts_IAM.service.userservice;

import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.AuditLogDtoForUser;
import com.guts.Guts_IAM.security.signup.UserRequestDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    private final AuditRepository auditRepository;

    public UserResponseDto viewProfile(Principal principal) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException("Login and Try Again")
        );

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        return userResponseDto;
    }


    public UserResponseDto updateProfile(UserRequestDto userRequestDto, Principal principal) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException(principal.getName()+" not found ,Login and try")
        );

        if(userRequestDto.getUserName()!=null){
            userExisting.setUserName(userRequestDto.getUserName());
        }

        userRepository.save(userExisting);

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        return userResponseDto;
    }

    public Page<AuditLogDtoForUser> viewLogs(Principal principal, Pageable pageable) {

        User user=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException("User not Found,Login and try"));



        Set<String> allowedSort=Set.of("auditedOn");

        pageable.getSort().forEach(order -> {
            if (!allowedSort.contains(order.getProperty())){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort Field "+order.getProperty()
                );
            }
        });

        Page<AuditLog> auditLogs=auditRepository.findByUserMail(pageable,principal.getName());
        return auditLogs.map(AuditLogDtoForUser::new);


    }
}
