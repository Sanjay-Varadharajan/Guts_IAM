package com.guts.Guts_IAM.user.service.admin;


import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.role.dto.RoleRequestDto;
import com.guts.Guts_IAM.role.dto.RoleResponseDto;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.role.repository.RoleRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.user.dto.admin.AdminRequestDto;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllActiveUsers(Authentication authentication, Pageable pageable, HttpServletRequest request) {
        User loggedInUser=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
                ()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)
        );

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


    public UserResponseDto updateUserStatus(Integer userId, Authentication authentication, HttpServletRequest request) {

        User loggedInAdmin = userRepository.findByUserMailAndActiveTrue(authentication.getName())
                .orElseThrow(
                        ()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)
                );

                        User user = userRepository.findById(userId).orElseThrow((                ()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)

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

    public Page<AuditLogDto> getAllAuditLog(Authentication authentication, Pageable pageable, HttpServletRequest request) {

        User adminCheck=userRepository.findByUserMailAndActiveTrue(authentication.getName()).
                orElseThrow(()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)
                );

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

    public UserResponseDto viewProfile(Authentication authentication, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
                ()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)
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

    public UserResponseDto updateProfile(AdminRequestDto adminRequestDto, Authentication authentication, HttpServletRequest request) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
                ()->new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND)

        );

        if(adminRequestDto.getAdminName()!=null){
            userExisting.setUserName(adminRequestDto.getAdminName());
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


    public RoleResponseDto addRoles(RoleRequestDto dto, HttpServletRequest httpServletRequest, Authentication authentication) {

        User admin=userRepository.findByUserMailAndActiveTrue(authentication.getName()).orElseThrow(
                ()->new UserNameNotFoundException(
                        "admin not found",
                "NOT_FOUND",
                HttpStatus.NOT_FOUND
        ));

        Role role=new Role();
        role.setName(dto.getRoleName());
        roleRepository.save(role);

        RoleResponseDto responseDto=new RoleResponseDto(role);

        AuditLog auditLog=new AuditLog();
        auditLog.setRoleName(admin.getRoles().toString());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        auditLog.setLogAction("PROFILE_UPDATED");
        auditLog.setResource("UPDATE");
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserMail(admin.getUserMail());
        auditLog.setResourceId(admin.getUserId().toString());
        auditRepository.save(auditLog);

        return responseDto;
    }
}
