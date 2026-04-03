package com.guts.Guts_IAM.service.userservice;

import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.AuditLogDtoForUser;
import com.guts.Guts_IAM.security.signup.UserRequestDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
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

    public UserResponseDto viewProfile(Principal principal, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException("Login and Try Again")
        );

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(userExisting.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("PROFILE_VIEWED");
        auditLog.setResource("VIEW");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(userExisting.getUserMail());
        auditLog.setResourceId(userExisting.getUserId().toString());
        auditRepository.save(auditLog);


        return userResponseDto;
    }


    public UserResponseDto updateProfile(UserRequestDto userRequestDto, Principal principal, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException(principal.getName()+" not found ,Login and try")
        );

        if(userRequestDto.getUserName()!=null){
            userExisting.setUserName(userRequestDto.getUserName());
        }

        userRepository.save(userExisting);

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(userExisting.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("PROFILE_UPDATED");
        auditLog.setResource("UPDATE");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(userExisting.getUserMail());
        auditLog.setResourceId(userExisting.getUserId().toString());
        auditRepository.save(auditLog);

        return userResponseDto;
    }

    public Page<AuditLogDtoForUser> viewLogs(Principal principal, Pageable pageable, HttpServletRequest request) {

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

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(user.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("LOG_VIEWED");
        auditLog.setResource("VIEW");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(user.getUserMail());
        auditLog.setResourceId(user.getUserId().toString());
        auditRepository.save(auditLog);

        Page<AuditLog> auditLogs=auditRepository.findByUserMail(pageable,principal.getName());
        return auditLogs.map(AuditLogDtoForUser::new);
    }
}
