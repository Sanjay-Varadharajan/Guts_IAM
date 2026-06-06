package com.guts.Guts_IAM.user.export;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.user.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProfileExportService {


    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    public String downloadProfile(
            Authentication authentication,
            HttpServletRequest httpServletRequest
            ) throws Exception {

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

        User user=userExisting.get();


        UserResponseDto userResponseDto=new UserResponseDto(user);
        String json =
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(userResponseDto);

        auditLogService.log(
                user,
                Action.DOWNLOAD_USER_PROFILE,
                "USER",
                authentication.getName(),
                AuditStatus.SUCCESS,
                "Profile Downloaded Successfully",
                httpServletRequest
        );

        return json;

}
}