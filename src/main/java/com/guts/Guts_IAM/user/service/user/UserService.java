package com.guts.Guts_IAM.user.service.user;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.export.service.DownloadAuditLogService;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static io.jsonwebtoken.Jwts.header;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    private final AuditRepository auditRepository;

    private final AuditLogService auditLogService;

    private final DownloadAuditLogService downloadAuditLogService;

    public UserResponseDto viewProfile(Authentication authentication, HttpServletRequest request) {

        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){

            auditLogService.log(
                    null,
                    Action.VIEW_PROFILE,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User userExisting=userExisting1.get();

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);



        auditLogService.log(
                userExisting,
                Action.VIEW_PROFILE,
                "USER",
                userExisting.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Profile viewed successfully",
                request
        );

        return userResponseDto;
    }


    public UserResponseDto updateProfile(UserRequestDto userRequestDto, Authentication authentication, HttpServletRequest request) {


        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){

            auditLogService.log(
                    null,
                    Action.UPDATE_PROFILE,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User userExisting=userExisting1.get();


        String oldUserName = userExisting.getUserName();

        boolean updated = false;

        if(userRequestDto.getUserName() != null &&
                !userRequestDto.getUserName().isBlank()) {

            userExisting.setUserName(userRequestDto.getUserName());
            updated = true;
        }

        if(!updated){

            auditLogService.log(
                    userExisting,
                    Action.UPDATE_PROFILE,
                    "USER",
                    userExisting.getUserId().toString(),
                    AuditStatus.FAILED,
                    "No valid fields provided for update",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No valid fields provided for update"
            );
        }

        userRepository.save(userExisting);

        auditLogService.log(
                userExisting,
                Action.UPDATE_PROFILE,
                "USER",
                userExisting.getUserId().toString(),
                AuditStatus.SUCCESS,
                "UserName changed from "
                        + oldUserName
                        + " to "
                        + userExisting.getUserName(),
                request
        );

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);


        return userResponseDto;
    }

    public Page<AuditLogDtoForUser> viewLogs(Authentication authentication, Pageable pageable, HttpServletRequest request) {




        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){
            auditLogService.log(
                    null,
                    Action.VIEW_LOGS,
                    "AUDIT_LOG",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+"Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User user=userExisting1.get();

                Set<String> allowedSort=Set.of("auditedOn");

        pageable.getSort().forEach(order -> {
            if (!allowedSort.contains(order.getProperty())){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort Field "+order.getProperty()
                );
            }
        });


        Page<AuditLog> auditLogs=auditRepository.findByUserMail(pageable,authentication.getName());

        auditLogService.log(
                user,
                Action.VIEW_LOGS,
                "AUDIT_LOG",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "logs viewed successfully",
                request
        );

        return auditLogs.map(AuditLogDtoForUser::new);
    }

    public InputStreamResource downloadMyLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        Optional<User> userExisting =
                userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.DOWNLOAD_AUDIT_LOG,
                    "AUDIT_LOG",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "No profile found",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    authentication.getName() + " Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        ByteArrayInputStream file =
                downloadAuditLogService.downloadMyLogs(authentication.getName());

        User user=userExisting.get();

        auditLogService.log(
                user,
                Action.DOWNLOAD_AUDIT_LOG,
                "AUDIT_LOG",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Audit log downloaded successfully",
                httpServletRequest
        );

        return new InputStreamResource(file);
    }
    }