package com.guts.Guts_IAM.service.adminservice;


import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.AuditLogDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
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

    private final AuditRepository auditRepository;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllActiveUsers(Principal principal, Pageable pageable, HttpServletRequest request) {
        User loggedInUser=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(()->
                new UsernameNotFoundException("User with this mail "+principal.getName()+" does not exist"));


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
        Page<User> activeUsers=userRepository.findByActiveTrue(pageable);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(loggedInUser.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("VIEW_ALL_ACTIVE_USER");
        auditLog.setResource("VIEW");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(loggedInUser.getUserMail());
        auditLog.setResourceId(loggedInUser.getUserId().toString());
        auditRepository.save(auditLog);

        return activeUsers.map(UserResponseDto::new);
    }


    public UserResponseDto updateUserStatus(Integer userId, Principal principal, HttpServletRequest request) {

        User loggedInAdmin = userRepository.findByUserMailAndActiveTrue(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Admin not found"
                ));

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(
                "User not found"
        ));

        if (loggedInAdmin.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You cannot change your own status");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(user.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("UPDATE_USER_STATUS");
        auditLog.setResource("UPDATE");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(user.getUserMail());
        auditLog.setResourceId(user.getUserId().toString());
        auditRepository.save(auditLog);

        return new UserResponseDto(user);
    }

    public Page<AuditLogDto> getAllAuditLog(Principal principal, Pageable pageable, HttpServletRequest request) {

        User adminCheck=userRepository.findByUserMailAndActiveTrue(principal.getName()).
                orElseThrow(()->new UsernameNotFoundException("User with this "+principal.getName()+" does not Exists"));

        Set<String> allowedSort=Set.of("auditedOn");

        pageable.getSort().forEach(order -> {
            if (!allowedSort.contains(order.getProperty())){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort Field "+order.getProperty()
                );
            }
        });

        Page<AuditLog> auditLogs=auditRepository.findAll(pageable);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(adminCheck.getRoles().toString());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setLogAction("VIEW_ALL_AUDIT_LOG");
        auditLog.setResource("VIEW");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserMail(adminCheck.getUserMail());
        auditLog.setResourceId(adminCheck.getUserId().toString());
        auditRepository.save(auditLog);


        return auditLogs.map(AuditLogDto::new);
    }
}
