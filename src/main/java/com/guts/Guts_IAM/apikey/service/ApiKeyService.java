package com.guts.Guts_IAM.apikey.service;


import com.guts.Guts_IAM.apikey.model.ApiKey;
import com.guts.Guts_IAM.apikey.status.Status;
import com.guts.Guts_IAM.apikey.repo.ApikeyRepository;
import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.HandleMissingParamException;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import com.guts.Guts_IAM.geolocation.service.GeoIPService;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final RedisTemplate<String, String> redisTemplate;

    private final AuditLogService auditLogService;

    private final ApikeyRepository apikeyRepository;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final GeoIPService geoIPService;

    private final UserAgentAnalyzer agentAnalyzer;

    private static final String PREFIX = "api_key:";




                    public String generateApiKey(String owner, HttpServletRequest httpServletRequest) throws HandleMissingParamException {


                        if(owner==null || owner.isBlank()){
                            throw new HandleMissingParamException(
                                    "Required Param is Missing",
                                    "BAD_REQUEST",
                                    HttpStatus.BAD_REQUEST
                            );
                        }


                        User user=userRepository.findByUserMailAndActiveTrue(owner).orElseThrow(
                                ()->new UserNameNotFoundException(
                                        "user not found",
                                        "NOT_FOUND",
                                        HttpStatus.NOT_FOUND
                                )
                        );
                        ApiKey apiKeyObj=new ApiKey();
                        apiKeyObj.setUserId(user.getUserId());




                        String apiKey = "guts_" + UUID.randomUUID();

                        apiKeyObj.setHashedApiKey(HashUtil.sha256(apiKey));
                        apikeyRepository.save(apiKeyObj);

                        redisTemplate.opsForValue().set(PREFIX + apiKey, Status.ACTIVE.name());

                        redisTemplate.opsForValue().set(PREFIX + apiKey + ":owner", owner);

                        auditLogService.log(
                                null,
                                Action.GENERATED_AUDIT_LOG,
                                "API_KEY",
                                owner,
                                AuditStatus.SUCCESS,
                                "api key is generated successfully",
                                httpServletRequest
                        );

                        String ip=auditLogService.extractIp(httpServletRequest);
                        GeoLocation geo = geoIPService.getLocation(ip);

                        emailService.apiKeyMail(owner,user.getUserName(),ip,geo);

                        return apiKey;
                    }

    public void revokeApiKey(String apiKey, HttpServletRequest httpServletRequest, Authentication authentication) throws AccessDeniedException {

        if(apiKey==null || apiKey.isBlank()){
            throw new HandleMissingParamException(
                    "Required Param is Missing",
                    "BAD_REQUEST",
                    HttpStatus.BAD_REQUEST
            );
        }





        Optional<User> user = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (user.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "authenticated user not found",
                    httpServletRequest
            );
            throw new UserNameNotFoundException(
                    "user not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        String hashedKey=HashUtil.sha256(apiKey);

        Optional<ApiKey> apikeyModel=apikeyRepository.findByHashedApiKey(hashedKey);

        if(apikeyModel.isEmpty()){
            auditLogService.log(
                    user.get(),
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


        Integer userId=apikeyModel.get().getUserId();

        Optional<User> userOptional=userRepository.findById(userId);



        if(userOptional.isEmpty()){
            auditLogService.log(
                    null,
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "authenticated user not found",
                    httpServletRequest
            );
            throw new UserNameNotFoundException(
                    "user not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }
        User apiKeyOwner=userOptional.get();



        if(user.get().getUserId().equals(apiKeyOwner.getUserId())){

            auditLogService.log(
                    user.get(),
                    Action.REVOKE_API_KEY,
                    "API_KEY",
                    userId.toString(),
                    AuditStatus.FAILED,
                    "you cannot revoke your own api key",
                    httpServletRequest
            );


            throw new AccessDeniedException("Admin cannot revoke own API key");
        }

        String ip=auditLogService.extractIp(httpServletRequest);
        GeoLocation geo = geoIPService.getLocation(ip);


        String userAgentString =
                httpServletRequest.getHeader("User-Agent");



        UserAgent agent =
                agentAnalyzer.parse(userAgentString);

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

        auditLogService.log(
                user.get(),
                Action.REVOKE_API_KEY,
                "API_KEY",
                userId.toString(),
                AuditStatus.SUCCESS,
                "api key is revoked successfully",
                httpServletRequest
        );
    }

    public boolean exists(String apiKey) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + apiKey)
        );
    }

    public Map<String,Object> getallKeys(HttpServletRequest httpServletRequest) {

        Set<String> keys = redisTemplate.keys(PREFIX + "*");

        Map<String, Object> response = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                response.put(key, redisTemplate.opsForValue().get(key));
            }
        }

        auditLogService.log(
                null,
                Action.VIEW_API_KEYS,
                "API_KEY",
                httpServletRequest.getRequestId(),
                AuditStatus.SUCCESS,
                "api key is viewed successfully",
                httpServletRequest

        );

        return response;
    }

}
