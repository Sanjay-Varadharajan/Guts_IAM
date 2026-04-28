package com.guts.Guts_IAM.user.service.user;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    public UserResponseDto viewProfile(Authentication authentication, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
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


    public UserResponseDto updateProfile(UserRequestDto userRequestDto, Authentication authentication, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
                ()->new UsernameNotFoundException(authentication.getName()+" not found ,Login and try")
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

    public Page<AuditLogDtoForUser> viewLogs(Authentication authentication, Pageable pageable, HttpServletRequest request) {

        User user=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
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

        Page<AuditLog> auditLogs=auditRepository.findByUserMail(pageable,authentication.getName());
        return auditLogs.map(AuditLogDtoForUser::new);
    }
}
