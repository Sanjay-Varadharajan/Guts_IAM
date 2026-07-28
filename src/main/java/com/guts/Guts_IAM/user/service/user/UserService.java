package com.guts.Guts_IAM.user.service.user;

import com.guts.Guts_IAM.apikey.model.ApiKey;
import com.guts.Guts_IAM.apikey.repo.ApikeyRepository;
import com.guts.Guts_IAM.apikey.status.Status;
import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.export.service.DownloadAuditLogService;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.ForbiddenException;
import com.guts.Guts_IAM.common.exception.types.HandleMissingParamException;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import com.guts.Guts_IAM.geolocation.service.GeoIPService;
import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.stats.AdminStatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;



@Service
@RequiredArgsConstructor
public class UserService {
    private final AdminStatsService adminStatsService;


    private final UserRepository userRepository;

    private final AuditRepository auditRepository;

    private final AuditLogService auditLogService;

    private final DownloadAuditLogService downloadAuditLogService;

    private final ApikeyRepository apikeyRepository;

    private final EmailService emailService;

    private final RedisTemplate<String,String> redisTemplate;

    private static final String PREFIX = "api_key:";

    private final GeoIPService geoIPService;

    private final UserAgentAnalyzer userAgentAnalyzer;

    public UserResponseDto viewProfile(Authentication authentication, HttpServletRequest request) {

        Optional<User> userExisting1 = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting1.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.VIEW_PROFILE,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName() + " Not found", "NOT_FOUND", HttpStatus.NOT_FOUND);

        }

        User userExisting = userExisting1.get();

        UserResponseDto userResponseDto = new UserResponseDto(userExisting);


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


        Optional<User> userExisting1 = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting1.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.UPDATE_PROFILE,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName() + " Not found", "NOT_FOUND", HttpStatus.NOT_FOUND);

        }

        User userExisting = userExisting1.get();


        String oldUserName = userExisting.getUserName();

        boolean updated = false;

        if (userRequestDto.getUserName() != null &&
                !userRequestDto.getUserName().isBlank()) {

            userExisting.setUserName(userRequestDto.getUserName());
            updated = true;
        }

        if (!updated) {

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

        UserResponseDto userResponseDto = new UserResponseDto(userExisting);


        return userResponseDto;
    }

    public Page<AuditLogDtoForUser> viewLogs(Authentication authentication, Pageable pageable, HttpServletRequest request) {


        Optional<User> userExisting1 = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting1.isEmpty()) {
            auditLogService.log(
                    null,
                    Action.VIEW_LOGS,
                    "AUDIT_LOG",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no profile found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName() + "Not found", "NOT_FOUND", HttpStatus.NOT_FOUND);

        }

        User user = userExisting1.get();

        Set<String> allowedSort = Set.of("auditedOn");

        pageable.getSort().forEach(order -> {
            if (!allowedSort.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort Field " + order.getProperty()
                );
            }
        });


        Page<AuditLog> auditLogs = auditRepository.findByUserMail(pageable, authentication.getName());

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

        User user = userExisting.get();

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

    public UserResponseDto toggleUserStatus(Authentication authentication,
                                            HttpServletRequest request) {

        User user = userRepository.findByUserMail(authentication.getName())
                .orElseThrow(() -> {
                    auditLogService.log(
                            null,
                            Action.UPDATE_USER_STATUS,
                            "USER",
                            authentication.getName(),
                            AuditStatus.FAILED,
                            "User not found",
                            request
                    );
                    return new UserNameNotFoundException(
                            authentication.getName() + " Not found",
                            "NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        boolean newStatus = !user.isActive();
        boolean oldStatus = user.isActive();

        user.setActive(newStatus);
        userRepository.save(user);

        adminStatsService.updateUserStatusStats(oldStatus, newStatus);

        auditLogService.log(
                user,
                Action.UPDATE_USER_STATUS,
                "USER",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "User " + user.getUserMail() + " status changed to " +
                        (newStatus ? "ACTIVE" : "INACTIVE"),
                request
        );

        return new UserResponseDto(user);
    }

    public void revokeApiKey(String apiKey, Authentication authentication, HttpServletRequest httpServletRequest) {

        if(apiKey==null || apiKey.isBlank()){
            throw new HandleMissingParamException(
                    "Required Param is Missing",
                    "BAD_REQUEST",
                    HttpStatus.BAD_REQUEST
            );
        }

        Optional<User> userExisting =
                userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
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

        String hashedKey= HashUtil.sha256(apiKey);
        ApiKey apiKeyModel=apikeyRepository.findByhashedApiKey(hashedKey);

        Optional<ApiKey> apikeyModel=apikeyRepository.findByHashedApiKey(hashedKey);

        User apiKeyOwner=userExisting.get();
        if(apikeyModel.isEmpty()){
            auditLogService.log(
                    apiKeyOwner,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "api key not found",
                    httpServletRequest
            );
            throw new ResourceNotFoundException(
                    "apikey not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }
        if(!apiKeyOwner.getUserId().equals(apiKeyModel.getUserId())) {
            auditLogService.log(
                    apiKeyOwner,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    apiKeyOwner.getUserId().toString(),
                    AuditStatus.FAILED,
                    "you cannot revoke api key which does not belong to you",
                    httpServletRequest
            );

            throw new ForbiddenException(
                    "you cannot revoke api key which does not belong to you",
                    "FORBIDDEN",
                    HttpStatus.FORBIDDEN
            );
        }

            String ip=auditLogService.extractIp(httpServletRequest);
            GeoLocation geo = geoIPService.getLocation(ip);


            String userAgentString =
                    httpServletRequest.getHeader("User-Agent");



            UserAgent agent =
                    userAgentAnalyzer.parse(userAgentString);

            String browser =
                    agent.getValue("AgentName");
            String operatingSystem =
                    agent.getValue("OperatingSystemName");


            String device;

            if (userAgentString.contains("Mobile")) {
                device = "Mobile";
            } else if (userAgentString.contains("Tablet")) {
                device = "Tablet";
            } else {
                device = "Desktop";
            }


            redisTemplate.delete(PREFIX + apiKey);
            redisTemplate.delete(PREFIX + apiKey + ":owner");


            ApiKey key=apikeyModel.get();
            key.setKeyStatus(Status.INACTIVE);
            apikeyRepository.save(key);

            emailService.apiKeyRevokeMail(apiKeyOwner.getUserMail(),apiKeyOwner.getUserName(),ip,geo,device,browser,operatingSystem, LocalDateTime.now());

            System.out.print(apiKeyOwner.getUserMail());
            auditLogService.log(
                    apiKeyOwner,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    apiKeyOwner.getUserId().toString(),
                    AuditStatus.SUCCESS,
                    "api key is revoked successfully",
                    httpServletRequest
            );
        }

        }


